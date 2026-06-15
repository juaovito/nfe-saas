package br.com.nfesaas.api.dto.response;

import br.com.nfesaas.domain.enums.RegimeTributario;
import br.com.nfesaas.domain.model.Empresa;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class EmpresaResponse {
    public UUID id;
    public String cnpj;
    public String razaoSocial;
    public String nomeFantasia;
    public RegimeTributario regimeTributario;
    public String uf;
    public String municipio;
    public String email;
    public boolean ativo;
    public LocalDateTime criadoEm;

    public static EmpresaResponse from(Empresa e) {
        return EmpresaResponse.builder()
            .id(e.getId()).cnpj(e.getCnpj()).razaoSocial(e.getRazaoSocial())
            .nomeFantasia(e.getNomeFantasia()).regimeTributario(e.getRegimeTributario())
            .uf(e.getUf()).municipio(e.getMunicipio()).email(e.getEmail())
            .ativo(e.isAtivo()).criadoEm(e.getCriadoEm()).build();
    }
}