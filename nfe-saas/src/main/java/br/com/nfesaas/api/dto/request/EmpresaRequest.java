package br.com.nfesaas.api.dto.request;

import br.com.nfesaas.domain.enums.RegimeTributario;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EmpresaRequest {
    @NotBlank @Size(min = 14, max = 14) public String cnpj;
    @NotBlank public String razaoSocial;
    public String nomeFantasia;
    public String inscricaoEstadual;
    public String inscricaoMunicipal;
    @NotNull public RegimeTributario regimeTributario;
    @Size(max = 8) public String cep;
    public String logradouro;
    public String numero;
    public String complemento;
    public String bairro;
    public String municipio;
    @Size(max = 2) public String uf;
    public String codigoIbgeMunicipio;
    @Email public String email;
    public String telefone;
}