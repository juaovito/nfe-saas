package br.com.nfesaas.domain.repository;

import br.com.nfesaas.domain.model.CertificadoDigital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CertificadoDigitalRepository extends JpaRepository<CertificadoDigital, UUID> {

    Optional<CertificadoDigital> findByEmpresaId(UUID empresaId);
}
