package br.com.nfesaas.domain.model;

import br.com.nfesaas.domain.enums.TipoAmbiente;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "certificados_digitais")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CertificadoDigital extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    private Empresa empresa;

    /** Caminho/chave no vault ou S3 onde o .pfx criptografado está salvo. */
    @Column(nullable = false)
    private String storagePath;

    /** Senha do certificado criptografada com AES-256-GCM. */
    @Column(nullable = false)
    private String senhaCriptografada;

    @Column(nullable = false)
    private LocalDate dataValidade;

    private String numeroDeSerie;
    private String cnpjCertificado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAmbiente ambiente;
}
