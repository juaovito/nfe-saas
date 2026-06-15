package br.com.nfesaas.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "idx_cliente_empresa", columnList = "empresa_id"),
    @Index(name = "idx_cliente_cpf_cnpj", columnList = "cpf_cnpj")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Cliente extends BaseEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "cpf_cnpj", nullable = false, length = 14)
    private String cpfCnpj;

    @Column(nullable = false)
    private String nome;

    private String inscricaoEstadual;

    @Column(nullable = false)
    private boolean pessoaJuridica;

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

    @Column(nullable = false)
    private boolean consumidorFinal = false;
}
