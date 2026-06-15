package br.com.nfesaas.domain.repository;

import br.com.nfesaas.domain.model.EmpresaCredencial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaCredencialRepository extends JpaRepository<EmpresaCredencial, UUID> {
    Optional<EmpresaCredencial> findByEmpresaId(UUID empresaId);
}
