package br.com.nfesaas.api.controller;

import br.com.nfesaas.api.dto.request.EmpresaRequest;
import br.com.nfesaas.api.dto.response.EmpresaResponse;
import br.com.nfesaas.application.empresa.EmpresaService;
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
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Gerenciamento de empresas emitentes")
@SecurityRequirement(name = "bearerAuth")
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    @Operation(summary = "Cadastrar nova empresa")
    public ResponseEntity<EmpresaResponse> cadastrar(@Valid @RequestBody EmpresaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(EmpresaResponse.from(empresaService.cadastrar(req)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar empresa por ID")
    public ResponseEntity<EmpresaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(EmpresaResponse.from(empresaService.buscarPorId(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Dados da empresa autenticada")
    public ResponseEntity<EmpresaResponse> me() {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.ok(EmpresaResponse.from(empresaService.buscarPorId(empresaId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar empresa")
    public ResponseEntity<EmpresaResponse> atualizar(@PathVariable UUID id,
                                                      @Valid @RequestBody EmpresaRequest req) {
        return ResponseEntity.ok(EmpresaResponse.from(empresaService.atualizar(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar empresa")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        empresaService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
