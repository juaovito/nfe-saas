package br.com.nfesaas.api.controller;

import br.com.nfesaas.api.dto.request.NotaFiscalServicoRequest;
import br.com.nfesaas.api.dto.response.NotaFiscalServicoResponse;
import br.com.nfesaas.application.nfeservico.NotaFiscalServicoService;
import br.com.nfesaas.application.nfeservico.NfseTransmissaoService;
import br.com.nfesaas.domain.enums.StatusNota;
import br.com.nfesaas.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nfse")
@RequiredArgsConstructor
@Tag(name = "NFS-e", description = "Notas Fiscais de Serviço Eletrônica (Padrão Nacional)")
@SecurityRequirement(name = "bearerAuth")
public class NotaFiscalServicoController {

    private final NotaFiscalServicoService notaFiscalServicoService;
    private final NfseTransmissaoService nfseTransmissaoService;

    @GetMapping
    @Operation(summary = "Listar NFS-e da empresa autenticada, opcionalmente filtrando por status")
    public ResponseEntity<List<NotaFiscalServicoResponse>> listar(
            @RequestParam(required = false) StatusNota status) {
        UUID empresaId = TenantContext.get();
        List<NotaFiscalServicoResponse> lista = (status != null
                ? notaFiscalServicoService.listarPorStatus(empresaId, status)
                : notaFiscalServicoService.listarPorEmpresa(empresaId))
            .stream().map(NotaFiscalServicoResponse::from).toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    @Operation(summary = "Criar rascunho de NFS-e (sem transmitir)")
    public ResponseEntity<NotaFiscalServicoResponse> emitir(
            @Valid @RequestBody NotaFiscalServicoRequest req) {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(NotaFiscalServicoResponse.from(
                notaFiscalServicoService.emitir(empresaId, req)));
    }

    @PostMapping("/{id}/transmitir")
    @Operation(summary = "Transmitir NFS-e para a prefeitura (assina e envia)")
    public ResponseEntity<NotaFiscalServicoResponse> transmitir(@PathVariable UUID id) {
        UUID empresaId = TenantContext.get();
        NotaFiscalServicoResponse resposta = NotaFiscalServicoResponse.from(
                nfseTransmissaoService.transmitir(empresaId, id));
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar NFS-e por ID")
    public ResponseEntity<NotaFiscalServicoResponse> buscar(@PathVariable UUID id) {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.ok(NotaFiscalServicoResponse.from(
                notaFiscalServicoService.buscarPorId(empresaId, id)));
    }

    @GetMapping("/chave/{chaveAcesso}")
    @Operation(summary = "Buscar NFS-e pela chave de acesso")
    public ResponseEntity<NotaFiscalServicoResponse> buscarPorChave(
            @PathVariable String chaveAcesso) {
        return ResponseEntity.ok(NotaFiscalServicoResponse.from(
                notaFiscalServicoService.buscarPorChave(chaveAcesso)));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar NFS-e autorizada")
    public ResponseEntity<NotaFiscalServicoResponse> cancelar(
            @PathVariable UUID id,
            @RequestBody CancelamentoRequest req) {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.ok(NotaFiscalServicoResponse.from(
                notaFiscalServicoService.cancelar(empresaId, id, req.justificativa)));
    }

    public static class CancelamentoRequest {
        public String justificativa;
    }
}
