package br.com.nfesaas.domain.model;

import br.com.nfesaas.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa a Declaração de Prestação de Serviços (DPS) e a respectiva
 * NFS-e emitida no padrão nacional (ADN - Ambiente de Dados Nacional).
 */
@Entity
@Table(name = "notas_fiscais_servico", indexes = {
    @Index(name = "idx_nfse_empresa", columnList = "empresa_id"),
    @Index(name = "idx_nfse_chave_acesso", columnList = "chave_acesso", unique = true),
    @Index(name = "idx_nfse_status", columnList = "status")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class NotaFiscalServico extends BaseEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNota tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAmbiente ambiente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusNota status;

    // ---- Identificação da DPS ----

    /** Série da DPS definida pelo prestador. */
    @Column(name = "serie_dps", nullable = false, length = 5)
    private String serieDps;

    /** Número sequencial da DPS gerado pelo prestador. */
    @Column(name = "numero_dps", nullable = false)
    private Long numeroDps;

    /** Identificador único da DPS (chave de acesso do XML da DPS). */
    @Column(name = "chave_acesso_dps", length = 50)
    private String chaveAcessoDps;

    @Column(nullable = false)
    private LocalDateTime dataEmissao;

    /** Competência (data de referência da prestação do serviço). */
    @Column(nullable = false)
    private LocalDate competencia;

    // ---- Local da prestação ----

    @Column(name = "codigo_municipio_prestacao", nullable = false, length = 7)
    private String codigoMunicipioPrestacao;

    /** Código do local da prestação conforme tabela do Anexo (ex: País ISO para exterior). */
    @Column(name = "codigo_pais_prestacao", length = 4)
    private String codigoPaisPrestacao;

    // ---- Tributação ----

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NaturezaTributacao naturezaTributacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegimeTributario regimeTributario;

    /** Indica se a empresa é optante pelo Simples Nacional na data de emissão. */
    @Column(name = "optante_simples_nacional", nullable = false)
    private boolean optanteSimplesNacional = false;

    /** Código do regime especial de tributação, conforme tabela ABRASF/ADN. */
    @Column(name = "regime_especial_tributacao")
    private Integer regimeEspecialTributacao;

    // ---- NFS-e (emitida pela SEFAZ Nacional / ADN) ----

    /** Chave de acesso da NFS-e gerada (44 posições). */
    @Column(name = "chave_acesso", unique = true, length = 50)
    private String chaveAcesso;

    /** Número da NFS-e atribuído pelo Ambiente de Dados Nacional. */
    private Long numeroNfse;

    /** Código de verificação da NFS-e. */
    @Column(length = 9)
    private String codigoVerificacao;

    private LocalDateTime dataAutorizacao;

    // ---- Valores totais ----

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorServicos;

    private BigDecimal valorDeducoes;
    private BigDecimal valorDescontoIncondicionado;
    private BigDecimal valorDescontoCondicionado;

    @Column(name = "base_calculo_iss", precision = 15, scale = 2)
    private BigDecimal baseCalculoIss;

    @Column(name = "valor_iss", precision = 15, scale = 2)
    private BigDecimal valorIss;

    @Column(name = "valor_total_nota", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotalNota;

    private BigDecimal valorPis;
    private BigDecimal valorCofins;
    private BigDecimal valorIr;
    private BigDecimal valorInss;
    private BigDecimal valorCsll;

    // ---- XML / Assinatura / Transmissão ----

    @Column(columnDefinition = "TEXT")
    private String xmlDps;

    @Column(columnDefinition = "TEXT")
    private String xmlDpsAssinado;

    @Column(columnDefinition = "TEXT")
    private String xmlNfse;

    private String numeroProtocolo;

    @Column(columnDefinition = "TEXT")
    private String motivoRejeicao;

    @Column(columnDefinition = "TEXT")
    private String informacoesComplementares;

    @OneToMany(mappedBy = "notaFiscalServico", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ItemServicoNota> itens = new ArrayList<>();
}
