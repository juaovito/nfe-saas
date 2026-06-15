package br.com.nfesaas.domain.repository;

import br.com.nfesaas.domain.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, UUID> {
    List<Servico> findAllByEmpresaId(UUID empresaId);
    List<Servico> findAllByEmpresaIdAndCodigoTributacaoNacional(UUID empresaId, String codigoTributacaoNacional);
}
