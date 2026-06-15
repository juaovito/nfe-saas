package br.com.nfesaas.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteRequest {
    @NotBlank @Size(min = 11, max = 14) public String cpfCnpj;
    @NotBlank public String nome;
    public String inscricaoEstadual;
    @NotNull public Boolean pessoaJuridica;
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
    public Boolean consumidorFinal = false;
}
