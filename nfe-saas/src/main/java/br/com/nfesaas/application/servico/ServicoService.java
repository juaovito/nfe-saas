package br.com.nfesaas.application.servico;

import br.com.nfesaas.api.dto.request.ServicoRequest;
import br.com.nfesaas.domain.model.Servico;
import br.com.nfesaas.domain.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public List<Servico> listarPorEmpresa(UUID empresaId) {
        return servicoRepository.findAllByEmpresaId(empresaId);
    }

    public Servico buscarPorId(UUID empresaId, UUID servicoId) {
        return servicoRepository.findById(servicoId)
            .filter(s -> s.getEmpresaId().equals(empresaId))
            .orElseThrow(() -> new RuntimeException("Serviço não encontrado: " + servicoId));
    }

    @Transactional
    public Servico cadastrar(UUID empresaId, ServicoRequest req) {
        Servico servico = Servico.builder()
            .empresaId(empresaId)
            .descricao(req.getDescricao())
            .codigoInterno(req.getCodigoInterno())
            .codigoTributacaoNacional(req.getCodigoTributacaoNacional())
            .codigoTributacaoMunicipal(req.getCodigoTributacaoMunicipal())
            .itemListaServico(req.getItemListaServico())
            .cnae(req.getCnae())
            .valorUnitario(req.getValorUnitario())
            .unidadeCodigo(req.getUnidadeCodigo())
            .aliquotaIss(req.getAliquotaIss())
            .issRetido(req.isIssRetido())
            .reducaoBaseIss(req.getReducaoBaseIss())
            .build();
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico atualizar(UUID empresaId, UUID servicoId, ServicoRequest req) {
        Servico servico = buscarPorId(empresaId, servicoId);
        servico.setDescricao(req.getDescricao());
        servico.setCodigoInterno(req.getCodigoInterno());
        servico.setCodigoTributacaoNacional(req.getCodigoTributacaoNacional());
        servico.setCodigoTributacaoMunicipal(req.getCodigoTributacaoMunicipal());
        servico.setItemListaServico(req.getItemListaServico());
        servico.setCnae(req.getCnae());
        servico.setValorUnitario(req.getValorUnitario());
        servico.setUnidadeCodigo(req.getUnidadeCodigo());
        servico.setAliquotaIss(req.getAliquotaIss());
        servico.setIssRetido(req.isIssRetido());
        servico.setReducaoBaseIss(req.getReducaoBaseIss());
        return servicoRepository.save(servico);
    }

    @Transactional
    public void inativar(UUID empresaId, UUID servicoId) {
        Servico servico = buscarPorId(empresaId, servicoId);
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }
}
