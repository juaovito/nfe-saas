package br.com.nfesaas.application.nfeservico;

import br.com.nfesaas.api.exception.BusinessException;
import br.com.nfesaas.api.exception.EntityNotFoundException;
import br.com.nfesaas.domain.enums.StatusNota;
import br.com.nfesaas.domain.model.Cliente;
import br.com.nfesaas.domain.model.Empresa;
import br.com.nfesaas.domain.model.NotaFiscalServico;
import br.com.nfesaas.domain.repository.ClienteRepository;
import br.com.nfesaas.domain.repository.EmpresaRepository;
import br.com.nfesaas.domain.repository.NotaFiscalServicoRepository;
import br.com.nfesaas.infrastructure.sefaz.SefazClient;
import br.com.nfesaas.infrastructure.sefaz.SefazResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orquestra o fluxo completo de transmissão de uma NFS-e:
 *
 *  1. Valida que a nota está no status correto (RASCUNHO)
 *  2. Gera o XML da DPS  (DpsXmlGenerator)
 *  3. Assina o XML       (DpsXmlSigner)
 *  4. Envia para a prefeitura (SefazClient)
 *  5. Atualiza o status da nota conforme o retorno
 *
 * Este serviço é chamado pelo NotaFiscalServicoController no endpoint
 * POST /empresas/{empresaId}/notas/{notaId}/transmitir
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NfseTransmissaoService {

    private final NotaFiscalServicoRepository notaRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final DpsXmlGenerator xmlGenerator;
    private final DpsXmlSigner xmlSigner;
    private final SefazClient sefazClient;

    /**
     * Transmite uma nota que está em status RASCUNHO para a prefeitura.
     *
     * @param empresaId ID da empresa
     * @param notaId    ID da nota a transmitir
     * @return NotaFiscalServico atualizada com o resultado
     */
    @Transactional
    public NotaFiscalServico transmitir(UUID empresaId, UUID notaId) {

        // 1. Carrega a nota e valida o status
        NotaFiscalServico nota = notaRepository.findById(notaId)
                .filter(n -> n.getEmpresaId().equals(empresaId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nota não encontrada: " + notaId));

        if (nota.getStatus() != StatusNota.RASCUNHO
                && nota.getStatus() != StatusNota.REJEITADA) {
            throw new BusinessException(
                    "Apenas notas com status RASCUNHO ou REJEITADA podem ser transmitidas. "
                    + "Status atual: " + nota.getStatus());
        }

        // 2. Carrega empresa e cliente
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Empresa não encontrada: " + empresaId));

        Cliente cliente = clienteRepository.findById(nota.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente não encontrado: " + nota.getClienteId()));

        // 3. Gera o XML da DPS
        log.info("Gerando XML da DPS — nota {} empresa {}", notaId, empresaId);
        nota.setStatus(StatusNota.VALIDANDO);
        String xmlDps = xmlGenerator.gerar(nota, empresa, cliente);
        nota.setXmlDps(xmlDps);

        // 4. Assina o XML com o certificado A1 da empresa
        log.info("Assinando XML da DPS — nota {}", notaId);
        nota.setStatus(StatusNota.ASSINADA);
        String xmlAssinado = xmlSigner.assinar(xmlDps, empresaId);
        nota.setXmlDpsAssinado(xmlAssinado);

        // 5. Envia para a prefeitura
        log.info("Transmitindo NFS-e para a prefeitura — nota {} ambiente {}",
                notaId, nota.getAmbiente());
        nota.setStatus(StatusNota.TRANSMITINDO);
        notaRepository.save(nota); // persiste o status intermediário

        SefazResponse resposta = sefazClient.enviarLote(xmlAssinado, nota.getAmbiente());

        // 6. Processa o retorno
        if (resposta.isAutorizada()) {
            log.info("NFS-e autorizada — nota {} protocolo {} número {}",
                    notaId, resposta.getNProt(), resposta.getNNFe());

            nota.setStatus(StatusNota.AUTORIZADA);
            nota.setNumeroProtocolo(resposta.getNProt());
            nota.setChaveAcesso(resposta.getChNFe());

            if (resposta.getNNFe() != null) {
                try {
                    nota.setNumeroNfse(Long.parseLong(resposta.getNNFe()));
                } catch (NumberFormatException ignored) {
                    // número veio em formato inesperado — apenas loga
                    log.warn("Número da NFS-e em formato inesperado: {}", resposta.getNNFe());
                }
            }

            nota.setDataAutorizacao(LocalDateTime.now());
            nota.setXmlNfse(resposta.getXmlRetorno());
            nota.setMotivoRejeicao(null);

        } else {
            log.warn("NFS-e rejeitada — nota {} cStat {} motivo {}",
                    notaId, resposta.getCStat(), resposta.getXMotivo());

            nota.setStatus(StatusNota.REJEITADA);
            nota.setMotivoRejeicao(
                    "[" + resposta.getCStat() + "] " + resposta.getXMotivo());
        }

        return notaRepository.save(nota);
    }
}
