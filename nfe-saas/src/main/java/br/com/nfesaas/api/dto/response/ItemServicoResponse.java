package br.com.nfesaas.api.dto.response;

import br.com.nfesaas.domain.model.ItemServicoNota;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder
public class ItemServicoResponse {
    public UUID id;
    public UUID servicoId;
    public Integer numeroItem;
    public String descricao;
    public String codigoTributacaoNacional;
    public String codigoTributacaoMunicipal;
    public BigDecimal quantidade;
    public BigDecimal valorUnitario;
    public BigDecimal valorServico;
    public BigDecimal valorDescontoIncondicionado;
    public BigDecimal valorDescontoCondicionado;
    public BigDecimal valorDeducoes;
    public BigDecimal baseCalculoIss;
    public BigDecimal aliquotaIss;
    public BigDecimal valorIss;
    public boolean issRetido;
    public BigDecimal valorPis;
    public BigDecimal valorCofins;
    public BigDecimal valorIr;
    public BigDecimal valorInss;
    public BigDecimal valorCsll;

    public static ItemServicoResponse from(ItemServicoNota i) {
        return ItemServicoResponse.builder()
            .id(i.getId())
            .servicoId(i.getServicoId())
            .numeroItem(i.getNumeroItem())
            .descricao(i.getDescricao())
            .codigoTributacaoNacional(i.getCodigoTributacaoNacional())
            .codigoTributacaoMunicipal(i.getCodigoTributacaoMunicipal())
            .quantidade(i.getQuantidade())
            .valorUnitario(i.getValorUnitario())
            .valorServico(i.getValorServico())
            .valorDescontoIncondicionado(i.getValorDescontoIncondicionado())
            .valorDescontoCondicionado(i.getValorDescontoCondicionado())
            .valorDeducoes(i.getValorDeducoes())
            .baseCalculoIss(i.getBaseCalculoIss())
            .aliquotaIss(i.getAliquotaIss())
            .valorIss(i.getValorIss())
            .issRetido(i.isIssRetido())
            .valorPis(i.getValorPis())
            .valorCofins(i.getValorCofins())
            .valorIr(i.getValorIr())
            .valorInss(i.getValorInss())
            .valorCsll(i.getValorCsll())
            .build();
    }
}
