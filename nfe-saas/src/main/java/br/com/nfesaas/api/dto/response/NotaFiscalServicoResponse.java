package br.com.nfesaas.api.dto.response;

import br.com.nfesaas.domain.enums.*;
import br.com.nfesaas.domain.model.NotaFiscalServico;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class NotaFiscalServicoResponse {
    public UUID id;
    public UUID clienteId;

    public TipoNota tipo;
    public TipoAmbiente ambiente;
    public StatusNota status;

    public String serieDps;
    public Long numeroDps;
    public String chaveAcessoDps;
    public LocalDateTime dataEmissao;
    public LocalDate competencia;

    public String codigoMunicipioPrestacao;
    public String codigoPaisPrestacao;

    public NaturezaTributacao naturezaTributacao;
    public RegimeTributario regimeTributario;
    public boolean optanteSimplesNacional;
    public Integer regimeEspecialTributacao;

    public String chaveAcesso;
    public Long numeroNfse;
    public String codigoVerificacao;
    public LocalDateTime dataAutorizacao;

    public BigDecimal valorServicos;
    public BigDecimal valorDeducoes;
    public BigDecimal valorDescontoIncondicionado;
    public BigDecimal valorDescontoCondicionado;
    public BigDecimal baseCalculoIss;
    public BigDecimal valorIss;
    public BigDecimal valorTotalNota;
    public BigDecimal valorPis;
    public BigDecimal valorCofins;
    public BigDecimal valorIr;
    public BigDecimal valorInss;
    public BigDecimal valorCsll;

    public String numeroProtocolo;
    public String motivoRejeicao;
    public String informacoesComplementares;

    public List<ItemServicoResponse> itens;

    public LocalDateTime criadoEm;

    public static NotaFiscalServicoResponse from(NotaFiscalServico n) {
        return NotaFiscalServicoResponse.builder()
            .id(n.getId())
            .clienteId(n.getClienteId())
            .tipo(n.getTipo())
            .ambiente(n.getAmbiente())
            .status(n.getStatus())
            .serieDps(n.getSerieDps())
            .numeroDps(n.getNumeroDps())
            .chaveAcessoDps(n.getChaveAcessoDps())
            .dataEmissao(n.getDataEmissao())
            .competencia(n.getCompetencia())
            .codigoMunicipioPrestacao(n.getCodigoMunicipioPrestacao())
            .codigoPaisPrestacao(n.getCodigoPaisPrestacao())
            .naturezaTributacao(n.getNaturezaTributacao())
            .regimeTributario(n.getRegimeTributario())
            .optanteSimplesNacional(n.isOptanteSimplesNacional())
            .regimeEspecialTributacao(n.getRegimeEspecialTributacao())
            .chaveAcesso(n.getChaveAcesso())
            .numeroNfse(n.getNumeroNfse())
            .codigoVerificacao(n.getCodigoVerificacao())
            .dataAutorizacao(n.getDataAutorizacao())
            .valorServicos(n.getValorServicos())
            .valorDeducoes(n.getValorDeducoes())
            .valorDescontoIncondicionado(n.getValorDescontoIncondicionado())
            .valorDescontoCondicionado(n.getValorDescontoCondicionado())
            .baseCalculoIss(n.getBaseCalculoIss())
            .valorIss(n.getValorIss())
            .valorTotalNota(n.getValorTotalNota())
            .valorPis(n.getValorPis())
            .valorCofins(n.getValorCofins())
            .valorIr(n.getValorIr())
            .valorInss(n.getValorInss())
            .valorCsll(n.getValorCsll())
            .numeroProtocolo(n.getNumeroProtocolo())
            .motivoRejeicao(n.getMotivoRejeicao())
            .informacoesComplementares(n.getInformacoesComplementares())
            .itens(n.getItens() == null ? List.of() :
                n.getItens().stream().map(ItemServicoResponse::from).toList())
            .criadoEm(n.getCriadoEm())
            .build();
    }
}
