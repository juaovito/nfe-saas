package br.com.nfesaas.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank public String cnpj;
    @NotBlank public String senha;
}
