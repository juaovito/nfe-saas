package br.com.nfesaas.application.auth;

import br.com.nfesaas.api.dto.request.AuthRequest;
import br.com.nfesaas.api.dto.response.AuthResponse;
import br.com.nfesaas.api.exception.BusinessException;
import br.com.nfesaas.api.exception.EntityNotFoundException;
import br.com.nfesaas.domain.model.Empresa;
import br.com.nfesaas.domain.model.EmpresaCredencial;
import br.com.nfesaas.domain.repository.EmpresaCredencialRepository;
import br.com.nfesaas.domain.repository.EmpresaRepository;
import br.com.nfesaas.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaCredencialRepository credencialRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.jwt.expiration-ms}")
    private long expirationMs;

    public AuthResponse autenticar(AuthRequest req) {
        Empresa empresa = empresaRepository.findByCnpj(req.getCnpj())
            .orElseThrow(() -> new EntityNotFoundException("CNPJ não encontrado"));

        if (!empresa.isAtivo())
            throw new BusinessException("Empresa inativa");

        EmpresaCredencial credencial = credencialRepository.findByEmpresaId(empresa.getId())
            .orElseThrow(() -> new BusinessException("Credenciais não configuradas para esta empresa"));

        if (!passwordEncoder.matches(req.getSenha(), credencial.getSenhaHash()))
            throw new BusinessException("Credenciais inválidas");

        String token = jwtService.gerarToken(empresa.getCnpj(), empresa.getId());
        return new AuthResponse(token, empresa.getCnpj(), empresa.getRazaoSocial(), expirationMs);
    }

    @Transactional
    public void definirSenha(String cnpj, String novaSenha) {
        Empresa empresa = empresaRepository.findByCnpj(cnpj)
            .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada: " + cnpj));

        EmpresaCredencial credencial = credencialRepository
            .findByEmpresaId(empresa.getId())
            .orElse(EmpresaCredencial.builder().empresaId(empresa.getId()).build());

        credencial.setSenhaHash(passwordEncoder.encode(novaSenha));
        credencialRepository.save(credencial);
    }
}
