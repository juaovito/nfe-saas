package br.com.nfesaas.domain.repository;

import br.com.nfesaas.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    List<Cliente> findAllByEmpresaId(UUID empresaId);
    Optional<Cliente> findByEmpresaIdAndCpfCnpj(UUID empresaId, String cpfCnpj);
}
