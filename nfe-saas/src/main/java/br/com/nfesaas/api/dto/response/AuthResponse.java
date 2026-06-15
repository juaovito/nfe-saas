package br.com.nfesaas.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class AuthResponse {
    public String token;
    public String cnpj;
    public String razaoSocial;
    public long expiresIn;
}
