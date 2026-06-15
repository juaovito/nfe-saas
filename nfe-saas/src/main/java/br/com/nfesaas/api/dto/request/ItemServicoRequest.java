package br.com.nfesaas.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ItemServicoRequest {
    @NotNull public UUID servicoId;

    @NotNull @DecimalMin("0.0001")
    public BigDecimal quantidade;

    /** Se não informado, usa o valorUnitario cadastrado no Servico. */
    public BigDecimal valorUnitario;

    /** Se não informado, usa a descrição cadastrada no Servico. */
    public String descricaoOverride;

    public BigDecimal valorDescontoIncondicionado;
    public BigDecimal valorDescontoCondicionado;
    public BigDecimal valorDeducoes;

    /** Se não informado, usa a alíquota cadastrada no Servico. */
    public BigDecimal aliquotaIss;

    /** Se não informado, usa o flag cadastrado no Servico. */
    public Boolean issRetido;
}
