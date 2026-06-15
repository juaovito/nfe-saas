package br.com.nfesaas.api.controller;

import br.com.nfesaas.api.dto.request.ClienteRequest;
import br.com.nfesaas.api.dto.response.ClienteResponse;
import br.com.nfesaas.application.cliente.ClienteService;
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
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes / destinatários de NF-e")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar clientes da empresa autenticada")
    public ResponseEntity<List<ClienteResponse>> listar() {
        UUID empresaId = TenantContext.get();
        List<ClienteResponse> lista = clienteService.listarPorEmpresa(empresaId)
            .stream().map(ClienteResponse::from).toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo cliente")
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest req) {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ClienteResponse.from(clienteService.cadastrar(empresaId, req)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClienteResponse> buscar(@PathVariable UUID id) {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.ok(ClienteResponse.from(clienteService.buscarPorId(empresaId, id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable UUID id,
                                                      @Valid @RequestBody ClienteRequest req) {
        UUID empresaId = TenantContext.get();
        return ResponseEntity.ok(ClienteResponse.from(clienteService.atualizar(empresaId, id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar cliente")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        UUID empresaId = TenantContext.get();
        clienteService.inativar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
