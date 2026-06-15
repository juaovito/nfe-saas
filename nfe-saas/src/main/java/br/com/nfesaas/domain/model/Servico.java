package br.com.nfesaas.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Item de serviço cadastrado pela empresa, utilizado para preencher
 * a Declaração de Prestação de Serviços (DPS) da NFS-e Padrão Nacional.
 */
@Entity
@Table(name = "servicos", indexes = {
    @Index(name = "idx_servico_empresa", columnList = "empresa_id"),
    @Index(name = "idx_servico_codigo_trib_nacional", columnList = "codigo_tributacao_nacional")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Servico extends BaseEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(nullable = false)
    private String descricao;

    private String codigoInterno;

    /**
     * Código de Tributação Nacional (cTribNac) - 6 dígitos, conforme
     * tabela do Anexo I do Manual da NFS-e Padrão Nacional (item LC 116/2003).
     */
    @Column(name = "codigo_tributacao_nacional", nullable = false, length = 6)
    private String codigoTributacaoNacional;

    /**
     * Código de Tributação Municipal (cTribMun), quando o município
     * exigir detalhamento adicional. Opcional.
     */
    @Column(name = "codigo_tributacao_municipal", length = 20)
    private String codigoTributacaoMunicipal;

    /** Código do item da Lista de Serviços (LC 116/2003), ex: "01.07". */
    @Column(name = "item_lista_servico", length = 7)
    private String itemListaServico;

    /** CNAE da atividade relacionada ao serviço. */
    @Column(length = 7)
    private String cnae;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal valorUnitario;

    @Column(name = "unidade_codigo", length = 6)
    private String unidadeCodigo;

    /** Alíquota do ISS aplicável a este serviço, em percentual (ex: 5.00 = 5%). */
    @Column(name = "aliquota_iss", precision = 5, scale = 2)
    private BigDecimal aliquotaIss;

    /** Indica se o ISS deve ser retido pelo tomador. */
    @Column(name = "iss_retido", nullable = false)
    private boolean issRetido = false;

    /** Percentual de redução da base de cálculo do ISS, se houver. */
    @Column(name = "reducao_base_iss", precision = 5, scale = 2)
    private BigDecimal reducaoBaseIss;
}
