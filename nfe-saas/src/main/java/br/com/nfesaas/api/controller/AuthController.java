package br.com.nfesaas.api.controller;

import br.com.nfesaas.api.dto.request.AuthRequest;
import br.com.nfesaas.api.dto.response.AuthResponse;
import br.com.nfesaas.application.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar empresa e obter token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.autenticar(req));
    }

    @PostMapping("/senha")
    @Operation(summary = "Definir ou alterar senha da empresa")
    public ResponseEntity<Void> definirSenha(@Valid @RequestBody AlterarSenhaRequest req) {
        authService.definirSenha(req.getCnpj(), req.getSenha());
        return ResponseEntity.noContent().build();
    }

    @Data
    static class AlterarSenhaRequest {
        @NotBlank public String cnpj;
        @NotBlank public String senha;
    }
}
