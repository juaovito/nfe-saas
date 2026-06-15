package br.com.nfesaas.application.empresa;

import br.com.nfesaas.api.dto.request.EmpresaRequest;
import br.com.nfesaas.api.exception.BusinessException;
import br.com.nfesaas.api.exception.EntityNotFoundException;
import br.com.nfesaas.domain.model.Empresa;
import br.com.nfesaas.domain.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public Empresa buscarPorId(UUID id) {
        return empresaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada: " + id));
    }

    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    @Transactional
    public Empresa cadastrar(EmpresaRequest req) {
        if (empresaRepository.existsByCnpj(req.getCnpj()))
            throw new BusinessException("CNPJ já cadastrado: " + req.getCnpj());

        Empresa empresa = Empresa.builder()
            .cnpj(req.getCnpj())
            .razaoSocial(req.getRazaoSocial())
            .nomeFantasia(req.getNomeFantasia())
            .inscricaoEstadual(req.getInscricaoEstadual())
            .inscricaoMunicipal(req.getInscricaoMunicipal())
            .regimeTributario(req.getRegimeTributario())
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
            .ultimoNumeroNfse(0L)
            .build();

        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa atualizar(UUID id, EmpresaRequest req) {
        Empresa empresa = buscarPorId(id);
        empresa.setRazaoSocial(req.getRazaoSocial());
        empresa.setNomeFantasia(req.getNomeFantasia());
        empresa.setInscricaoEstadual(req.getInscricaoEstadual());
        empresa.setInscricaoMunicipal(req.getInscricaoMunicipal());
        empresa.setRegimeTributario(req.getRegimeTributario());
        empresa.setCep(req.getCep());
        empresa.setLogradouro(req.getLogradouro());
        empresa.setNumero(req.getNumero());
        empresa.setComplemento(req.getComplemento());
        empresa.setBairro(req.getBairro());
        empresa.setMunicipio(req.getMunicipio());
        empresa.setUf(req.getUf());
        empresa.setCodigoIbgeMunicipio(req.getCodigoIbgeMunicipio());
        empresa.setEmail(req.getEmail());
        empresa.setTelefone(req.getTelefone());
        return empresaRepository.save(empresa);
    }

    @Transactional
    public void inativar(UUID id) {
        Empresa empresa = buscarPorId(id);
        empresa.setAtivo(false);
        empresaRepository.save(empresa);
    }
}
