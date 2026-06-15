package br.com.nfesaas.api.dto.response;

import br.com.nfesaas.domain.model.Servico;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class ServicoResponse {
    public UUID id;
    public String descricao;
    public String codigoInterno;
    public String codigoTributacaoNacional;
    public String codigoTributacaoMunicipal;
    public String itemListaServico;
    public String cnae;
    public BigDecimal valorUnitario;
    public String unidadeCodigo;
    public BigDecimal aliquotaIss;
    public boolean issRetido;
    public BigDecimal reducaoBaseIss;
    public boolean ativo;
    public LocalDateTime criadoEm;

    public static ServicoResponse from(Servico s) {
        return ServicoResponse.builder()
            .id(s.getId())
            .descricao(s.getDescricao())
            .codigoInterno(s.getCodigoInterno())
            .codigoTributacaoNacional(s.getCodigoTributacaoNacional())
            .codigoTributacaoMunicipal(s.getCodigoTributacaoMunicipal())
            .itemListaServico(s.getItemListaServico())
            .cnae(s.getCnae())
            .valorUnitario(s.getValorUnitario())
            .unidadeCodigo(s.getUnidadeCodigo())
            .aliquotaIss(s.getAliquotaIss())
            .issRetido(s.isIssRetido())
            .reducaoBaseIss(s.getReducaoBaseIss())
            .ativo(s.isAtivo())
            .criadoEm(s.getCriadoEm())
            .build();
    }
}
