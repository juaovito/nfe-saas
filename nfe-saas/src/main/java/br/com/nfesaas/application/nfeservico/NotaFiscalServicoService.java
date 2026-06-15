package br.com.nfesaas.application.nfeservico;

import br.com.nfesaas.api.dto.request.ItemServicoRequest;
import br.com.nfesaas.api.dto.request.NotaFiscalServicoRequest;
import br.com.nfesaas.api.exception.BusinessException;
import br.com.nfesaas.api.exception.EntityNotFoundException;
import br.com.nfesaas.domain.enums.StatusNota;
import br.com.nfesaas.domain.model.*;
import br.com.nfesaas.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotaFiscalServicoService {

    private static final String SERIE_DPS_PADRAO = "1";

    private final NotaFiscalServicoRepository notaFiscalServicoRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    public List<NotaFiscalServico> listarPorEmpresa(UUID empresaId) {
        return notaFiscalServicoRepository.findAllByEmpresaId(empresaId);
    }

    public List<NotaFiscalServico> listarPorStatus(UUID empresaId, StatusNota status) {
        return notaFiscalServicoRepository.findAllByEmpresaIdAndStatus(empresaId, status);
    }

    public NotaFiscalServico buscarPorId(UUID empresaId, UUID notaId) {
        return notaFiscalServicoRepository.findById(notaId)
            .filter(n -> n.getEmpresaId().equals(empresaId))
            .orElseThrow(() -> new EntityNotFoundException("Nota fiscal de serviço não encontrada: " + notaId));
    }

    public NotaFiscalServico buscarPorChave(String chaveAcesso) {
        return notaFiscalServicoRepository.findByChaveAcesso(chaveAcesso)
            .orElseThrow(() -> new EntityNotFoundException("Nota não encontrada para chave: " + chaveAcesso));
    }

    @Transactional
    public NotaFiscalServico emitir(UUID empresaId, NotaFiscalServicoRequest req) {
        // @Version na Empresa garante que dois requests simultâneos não leiam
        // o mesmo ultimoNumeroNfse — o segundo receberá OptimisticLockException.
        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));

        Cliente cliente = clienteRepository.findById(req.getClienteId())
            .filter(c -> c.getEmpresaId().equals(empresaId))
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + req.getClienteId()));

        long numeroDps = (empresa.getUltimoNumeroNfse() == null ? 0L : empresa.getUltimoNumeroNfse()) + 1;
        empresa.setUltimoNumeroNfse(numeroDps);
        empresaRepository.save(empresa);

        List<ItemServicoNota> itens = new ArrayList<>();
        BigDecimal valorServicos               = BigDecimal.ZERO;
        BigDecimal valorDeducoes               = BigDecimal.ZERO;
        BigDecimal valorDescontoIncondicionado = BigDecimal.ZERO;
        BigDecimal valorDescontoCondicionado   = BigDecimal.ZERO;
        BigDecimal baseCalculoIss              = BigDecimal.ZERO;
        BigDecimal valorIss                    = BigDecimal.ZERO;
        int numItem = 1;

        for (ItemServicoRequest itemReq : req.getItens()) {
            Servico servico = servicoRepository.findById(itemReq.getServicoId())
                .filter(s -> s.getEmpresaId().equals(empresaId))
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado: " + itemReq.getServicoId()));

            BigDecimal valorUnitario = itemReq.getValorUnitario() != null
                ? itemReq.getValorUnitario() : servico.getValorUnitario();

            BigDecimal valorServico = itemReq.getQuantidade()
                .multiply(valorUnitario)
                .setScale(2, RoundingMode.HALF_UP);

            BigDecimal descontoIncond = nz(itemReq.getValorDescontoIncondicionado());
            BigDecimal descontoCond   = nz(itemReq.getValorDescontoCondicionado());
            BigDecimal deducoes       = nz(itemReq.getValorDeducoes());

            BigDecimal aliquotaIss = itemReq.getAliquotaIss() != null
                ? itemReq.getAliquotaIss() : servico.getAliquotaIss();

            boolean issRetido = itemReq.getIssRetido() != null
                ? itemReq.getIssRetido() : servico.isIssRetido();

            BigDecimal baseItem = valorServico.subtract(deducoes).subtract(descontoIncond);
            if (servico.getReducaoBaseIss() != null) {
                BigDecimal fatorReducao = BigDecimal.ONE
                    .subtract(servico.getReducaoBaseIss().divide(BigDecimal.valueOf(100)));
                baseItem = baseItem.multiply(fatorReducao);
            }
            baseItem = baseItem.setScale(2, RoundingMode.HALF_UP);

            BigDecimal issItem = aliquotaIss != null
                ? baseItem.multiply(aliquotaIss).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            ItemServicoNota item = ItemServicoNota.builder()
                .servicoId(servico.getId())
                .numeroItem(numItem++)
                .descricao(itemReq.getDescricaoOverride() != null
                    ? itemReq.getDescricaoOverride() : servico.getDescricao())
                .codigoTributacaoNacional(servico.getCodigoTributacaoNacional())
                .codigoTributacaoMunicipal(servico.getCodigoTributacaoMunicipal())
                .quantidade(itemReq.getQuantidade())
                .valorUnitario(valorUnitario)
                .valorServico(valorServico)
                .valorDescontoIncondicionado(descontoIncond)
                .valorDescontoCondicionado(descontoCond)
                .valorDeducoes(deducoes)
                .baseCalculoIss(baseItem)
                .aliquotaIss(aliquotaIss)
                .valorIss(issItem)
                .issRetido(issRetido)
                .build();

            itens.add(item);

            valorServicos               = valorServicos.add(valorServico);
            valorDeducoes               = valorDeducoes.add(deducoes);
            valorDescontoIncondicionado = valorDescontoIncondicionado.add(descontoIncond);
            valorDescontoCondicionado   = valorDescontoCondicionado.add(descontoCond);
            baseCalculoIss              = baseCalculoIss.add(baseItem);
            valorIss                    = valorIss.add(issItem);
        }

        BigDecimal valorTotalNota = valorServicos
            .subtract(valorDeducoes)
            .subtract(valorDescontoIncondicionado)
            .subtract(valorDescontoCondicionado)
            .setScale(2, RoundingMode.HALF_UP);

        String codigoMunicipioPrestacao = req.getCodigoMunicipioPrestacao() != null
            ? req.getCodigoMunicipioPrestacao() : empresa.getCodigoIbgeMunicipio();

        NotaFiscalServico nota = NotaFiscalServico.builder()
            .empresaId(empresaId)
            .clienteId(cliente.getId())
            .tipo(req.getTipo())
            .ambiente(req.getAmbiente())
            .status(StatusNota.RASCUNHO)
            .serieDps(SERIE_DPS_PADRAO)
            .numeroDps(numeroDps)
            .dataEmissao(LocalDateTime.now())
            .competencia(req.getCompetencia())
            .codigoMunicipioPrestacao(codigoMunicipioPrestacao)
            .codigoPaisPrestacao(req.getCodigoPaisPrestacao())
            .naturezaTributacao(req.getNaturezaTributacao())
            .regimeTributario(empresa.getRegimeTributario())
            .optanteSimplesNacional(empresa.getRegimeTributario() != null
                && empresa.getRegimeTributario().name().startsWith("SIMPLES_NACIONAL"))
            .regimeEspecialTributacao(req.getRegimeEspecialTributacao())
            .valorServicos(valorServicos)
            .valorDeducoes(valorDeducoes)
            .valorDescontoIncondicionado(valorDescontoIncondicionado)
            .valorDescontoCondicionado(valorDescontoCondicionado)
            .baseCalculoIss(baseCalculoIss)
            .valorIss(valorIss)
            .valorTotalNota(valorTotalNota)
            .informacoesComplementares(req.getInformacoesComplementares())
            .itens(itens)
            .build();

        itens.forEach(i -> i.setNotaFiscalServico(nota));

        return notaFiscalServicoRepository.save(nota);
    }

    @Transactional
    public NotaFiscalServico cancelar(UUID empresaId, UUID notaId, String justificativa) {
        NotaFiscalServico nota = buscarPorId(empresaId, notaId);

        if (nota.getStatus() != StatusNota.AUTORIZADA)
            throw new BusinessException("Apenas notas AUTORIZADAS podem ser canceladas");

        if (justificativa == null || justificativa.trim().length() < 15)
            throw new BusinessException("Justificativa de cancelamento deve ter mínimo 15 caracteres");

        nota.setStatus(StatusNota.CANCELADA);
        nota.setMotivoRejeicao("CANCELAMENTO: " + justificativa);
        return notaFiscalServicoRepository.save(nota);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
