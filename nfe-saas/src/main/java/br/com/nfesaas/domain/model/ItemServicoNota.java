package br.com.nfesaas.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Item de serviço dentro de uma NotaFiscalServico (DPS).
 * Cada item referencia um Servico cadastrado e guarda os valores
 * efetivamente aplicados na emissão (podendo ter sido sobrescritos).
 */
@Entity
@Table(name = "itens_nota_fiscal_servico")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ItemServicoNota extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_fiscal_servico_id", nullable = false)
    private NotaFiscalServico notaFiscalServico;

    @Column(name = "servico_id", nullable = false)
    private UUID servicoId;

    @Column(nullable = false)
    private Integer numeroItem;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "codigo_tributacao_nacional", nullable = false, length = 6)
    private String codigoTributacaoNacional;

    @Column(name = "codigo_tributacao_municipal", length = 20)
    private String codigoTributacaoMunicipal;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal valorUnitario;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorServico;

    private BigDecimal valorDescontoIncondicionado;
    private BigDecimal valorDescontoCondicionado;
    private BigDecimal valorDeducoes;

    @Column(name = "base_calculo_iss", precision = 15, scale = 2)
    private BigDecimal baseCalculoIss;

    @Column(name = "aliquota_iss", precision = 5, scale = 2)
    private BigDecimal aliquotaIss;

    @Column(name = "valor_iss", precision = 15, scale = 2)
    private BigDecimal valorIss;

    @Column(name = "iss_retido", nullable = false)
    private boolean issRetido = false;

    private BigDecimal valorPis;
    private BigDecimal valorCofins;
    private BigDecimal valorIr;
    private BigDecimal valorInss;
    private BigDecimal valorCsll;
}
