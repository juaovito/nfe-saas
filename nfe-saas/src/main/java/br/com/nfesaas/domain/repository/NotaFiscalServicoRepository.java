package br.com.nfesaas.domain.repository;

import br.com.nfesaas.domain.enums.StatusNota;
import br.com.nfesaas.domain.model.NotaFiscalServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotaFiscalServicoRepository extends JpaRepository<NotaFiscalServico, UUID> {
    List<NotaFiscalServico> findAllByEmpresaId(UUID empresaId);
    List<NotaFiscalServico> findAllByEmpresaIdAndStatus(UUID empresaId, StatusNota status);
    Optional<NotaFiscalServico> findByChaveAcesso(String chaveAcesso);
    Optional<NotaFiscalServico> findByEmpresaIdAndSerieDpsAndNumeroDps(UUID empresaId, String serieDps, Long numeroDps);
}
