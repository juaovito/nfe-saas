package br.com.nfesaas.domain.model;

import br.com.nfesaas.domain.enums.RegimeTributario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresas")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Empresa extends BaseEntity {

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(nullable = false)
    private String razaoSocial;

    private String nomeFantasia;

    @Column(length = 15)
    private String inscricaoEstadual;

    @Column(length = 15)
    private String inscricaoMunicipal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegimeTributario regimeTributario;

    @Column(length = 8)
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String municipio;

    @Column(length = 2)
    private String uf;

    @Column(length = 7)
    private String codigoIbgeMunicipio;

    private String email;
    private String telefone;

    private Long ultimoNumeroNfse;

    /**
     * Controle de concorrência otimista.
     * Garante que duas threads não incrementem ultimoNumeroNfse simultaneamente,
     * evitando numeração duplicada de DPS.
     */
    @Version
    private Long versao;

    @OneToOne(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CertificadoDigital certificado;
}
