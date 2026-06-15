package br.com.nfesaas.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServicoRequest {
    @NotBlank public String descricao;
    public String codigoInterno;

    @NotBlank @Size(min = 6, max = 6)
    public String codigoTributacaoNacional;

    public String codigoTributacaoMunicipal;
    public String itemListaServico;
    public String cnae;

    @NotNull @DecimalMin("0.0001") public BigDecimal valorUnitario;
    public String unidadeCodigo;

    public BigDecimal aliquotaIss;
    public boolean issRetido;
    public BigDecimal reducaoBaseIss;
}
