package br.com.nfesaas.api.dto.response;

import br.com.nfesaas.domain.model.Cliente;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class ClienteResponse {
    public UUID id;
    public String cpfCnpj;
    public String nome;
    public boolean pessoaJuridica;
    public String municipio;
    public String uf;
    public String email;
    public boolean ativo;
    public LocalDateTime criadoEm;

    public static ClienteResponse from(Cliente c) {
        return ClienteResponse.builder()
            .id(c.getId()).cpfCnpj(c.getCpfCnpj()).nome(c.getNome())
            .pessoaJuridica(c.isPessoaJuridica()).municipio(c.getMunicipio())
            .uf(c.getUf()).email(c.getEmail())
            .ativo(c.isAtivo()).criadoEm(c.getCriadoEm()).build();
    }
}
