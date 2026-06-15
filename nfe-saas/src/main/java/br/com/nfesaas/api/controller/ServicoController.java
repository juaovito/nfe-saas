package br.com.nfesaas.api.controller;

import br.com.nfesaas.api.dto.request.ServicoRequest;
import br.com.nfesaas.api.dto.response.ServicoResponse;
import br.com.nfesaas.application.servico.ServicoService;
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
@RequestMapping("/api/v1/servicos")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Catálogo de serviços da empresa (NFS-e Padrão Nacional)")
@SecurityRequirement(name = "bearerAuth")
public class ServicoController {

    private final ServicoService servicoService;

    @GetMapping
    @Operation(summary = "Listar serviços da empresa autenticada")
    public ResponseEntity<List<ServicoResponse>> listar() {
        UUID empresaId = TenantContext.get();
        List<ServicoResponse> lista = servicoService.listarPorEmpresa(empresaId)
            .stream().map(ServicoResponse::from).toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo serviço")
    public ResponseEntity<ServicoResponse> cadastrar(@Valid @RequestBody ServicoRequest req) {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ServicoResponse.from(servicoService.cadastrar(empresaId, req)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID")
    public ResponseEntity<ServicoResponse> buscar(@PathVariable UUID id) {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.ok(ServicoResponse.from(servicoService.buscarPorId(empresaId, id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar serviço")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable UUID id,
                                                       @Valid @RequestBody ServicoRequest req) {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.ok(ServicoResponse.from(servicoService.atualizar(empresaId, id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar serviço")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        UUID empresaId = TenantContext.get();
        servicoService.inativar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
