package br.com.nfesaas.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresa_credenciais")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class EmpresaCredencial extends BaseEntity {

    @Column(name = "empresa_id", nullable = false, unique = true)
    private java.util.UUID empresaId;

    @Column(nullable = false)
    private String senhaHash;
}
