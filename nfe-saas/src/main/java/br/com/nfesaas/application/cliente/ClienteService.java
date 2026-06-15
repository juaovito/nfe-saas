package br.com.nfesaas.application.cliente;

import br.com.nfesaas.api.dto.request.ClienteRequest;
import br.com.nfesaas.domain.model.Cliente;
import br.com.nfesaas.domain.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> listarPorEmpresa(UUID empresaId) {
        return clienteRepository.findAllByEmpresaId(empresaId);
    }

    public Cliente buscarPorId(UUID empresaId, UUID clienteId) {
        return clienteRepository.findById(clienteId)
            .filter(c -> c.getEmpresaId().equals(empresaId))
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + clienteId));
    }

    @Transactional
    public Cliente cadastrar(UUID empresaId, ClienteRequest req) {
        clienteRepository.findByEmpresaIdAndCpfCnpj(empresaId, req.getCpfCnpj())
            .ifPresent(c -> { throw new RuntimeException("CPF/CNPJ já cadastrado para esta empresa"); });

        Cliente cliente = Cliente.builder()
            .empresaId(empresaId)
            .cpfCnpj(req.getCpfCnpj())
            .nome(req.getNome())
            .inscricaoEstadual(req.getInscricaoEstadual())
            .pessoaJuridica(req.getPessoaJuridica())
            .cep(req.getCep())
            .logradouro(req.getLogradouro())
            .numero(req.getNumero())
            .complemento(req.getComplemento())
            .bairro(req.getBairro())
            .municipio(req.getMunicipio())
            .uf(req.getUf())
            .codigoIbgeMunicipio(req.getCodigoIbgeMunicipio())
            .email(req.getEmail())
            .telefone(req.getTelefone())
            .consumidorFinal(req.getConsumidorFinal() != null && req.getConsumidorFinal())
            .build();

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(UUID empresaId, UUID clienteId, ClienteRequest req) {
        Cliente cliente = buscarPorId(empresaId, clienteId);
        cliente.setNome(req.getNome());
        cliente.setInscricaoEstadual(req.getInscricaoEstadual());
        cliente.setCep(req.getCep());
        cliente.setLogradouro(req.getLogradouro());
        cliente.setNumero(req.getNumero());
        cliente.setComplemento(req.getComplemento());
        cliente.setBairro(req.getBairro());
        cliente.setMunicipio(req.getMunicipio());
        cliente.setUf(req.getUf());
        cliente.setCodigoIbgeMunicipio(req.getCodigoIbgeMunicipio());
        cliente.setEmail(req.getEmail());
        cliente.setTelefone(req.getTelefone());
        if (req.getConsumidorFinal() != null) cliente.setConsumidorFinal(req.getConsumidorFinal());
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void inativar(UUID empresaId, UUID clienteId) {
        Cliente cliente = buscarPorId(empresaId, clienteId);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }
}
