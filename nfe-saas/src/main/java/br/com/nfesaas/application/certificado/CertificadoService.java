package br.com.nfesaas.application.certificado;

import br.com.nfesaas.domain.enums.TipoAmbiente;
import br.com.nfesaas.domain.model.CertificadoDigital;
import br.com.nfesaas.domain.model.Empresa;
import br.com.nfesaas.domain.repository.CertificadoDigitalRepository;
import br.com.nfesaas.domain.repository.EmpresaRepository;
import br.com.nfesaas.api.exception.EntityNotFoundException;
import br.com.nfesaas.infrastructure.crypto.AesEncryptionService;
import br.com.nfesaas.infrastructure.storage.CertificadoStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Enumeration;
import java.util.UUID;

/**
 * Gerencia o ciclo de vida do Certificado Digital A1 da empresa.
 *
 * Fluxo:
 *  1. Cliente faz upload do .pfx via endpoint REST
 *  2. uploadCertificado() valida, criptografa e persiste
 *  3. DpsXmlSigner chama carregarKeyStore() para obter o KeyStore em memória
 */
@Service
@RequiredArgsConstructor
public class CertificadoService {

    private final EmpresaRepository empresaRepository;
    private final CertificadoDigitalRepository certificadoDigitalRepository;
    private final AesEncryptionService aesEncryptionService;
    private final CertificadoStorageService certificadoStorageService;

    /**
     * Recebe o arquivo .pfx enviado pelo cliente, valida a senha,
     * criptografa e persiste no banco + storage.
     *
     * @param empresaId ID da empresa
     * @param pfxFile   arquivo .pfx enviado via multipart
     * @param senha     senha do certificado em texto puro (descartada após uso)
     * @param ambiente  PRODUCAO ou HOMOLOGACAO
     * @return CertificadoDigital persistido
     */
    @Transactional
    public CertificadoDigital uploadCertificado(UUID empresaId,
                                                 MultipartFile pfxFile,
                                                 String senha,
                                                 TipoAmbiente ambiente) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada: " + empresaId));

        byte[] pfxBytes;
        try {
            pfxBytes = pfxFile.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo .pfx", e);
        }

        // Valida se a senha está correta abrindo o KeyStore
        KeyStore ks = abrirKeyStore(pfxBytes, senha);

        // Extrai metadados do certificado
        X509Certificate cert = extrairCertificado(ks);
        LocalDate validade = cert.getNotAfter()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        String numeroDeSerie = cert.getSerialNumber().toString(16).toUpperCase();

        // Criptografa o arquivo .pfx com AES-256-GCM antes de salvar
        String pfxBase64 = Base64.getEncoder().encodeToString(pfxBytes);
        String pfxCriptografado = aesEncryptionService.criptografar(pfxBase64);
        byte[] pfxEncBytes = pfxCriptografado.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // Salva no storage e obtém o path
        String storagePath = certificadoStorageService.salvar(empresaId, pfxEncBytes);

        // Criptografa a senha
        String senhaCriptografada = aesEncryptionService.criptografar(senha);

        // Persiste ou atualiza o registro no banco
        CertificadoDigital certificado = certificadoDigitalRepository
                .findByEmpresaId(empresaId)
                .orElse(CertificadoDigital.builder().empresa(empresa).build());

        certificado.setStoragePath(storagePath);
        certificado.setSenhaCriptografada(senhaCriptografada);
        certificado.setDataValidade(validade);
        certificado.setNumeroDeSerie(numeroDeSerie);
        certificado.setCnpjCertificado(empresa.getCnpj());
        certificado.setAmbiente(ambiente);

        return certificadoDigitalRepository.save(certificado);
    }

    /**
     * Carrega o KeyStore do certificado A1 da empresa em memória.
     * Usado pelo DpsXmlSigner para assinar o XML.
     *
     * @param empresaId ID da empresa
     * @return KeyStore pronto para uso
     */
    public KeyStore carregarKeyStore(UUID empresaId) {
        CertificadoDigital certificado = certificadoDigitalRepository
                .findByEmpresaId(empresaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Certificado não encontrado para empresa: " + empresaId));

        // Carrega bytes criptografados do storage
        byte[] pfxEncBytes = certificadoStorageService.carregar(certificado.getStoragePath());

        // Descriptografa o .pfx
        String pfxCriptografado = new String(pfxEncBytes, java.nio.charset.StandardCharsets.UTF_8);
        String pfxBase64 = aesEncryptionService.descriptografar(pfxCriptografado);
        byte[] pfxBytes = Base64.getDecoder().decode(pfxBase64);

        // Descriptografa a senha
        String senha = aesEncryptionService.descriptografar(certificado.getSenhaCriptografada());

        return abrirKeyStore(pfxBytes, senha);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private KeyStore abrirKeyStore(byte[] pfxBytes, String senha) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new java.io.ByteArrayInputStream(pfxBytes), senha.toCharArray());
            return ks;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Não foi possível abrir o certificado. Verifique se o arquivo e a senha estão corretos.", e);
        }
    }

    private X509Certificate extrairCertificado(KeyStore ks) {
        try {
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (ks.isKeyEntry(alias)) {
                    return (X509Certificate) ks.getCertificate(alias);
                }
            }
            throw new RuntimeException("Nenhuma chave privada encontrada no certificado .pfx");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair certificado do KeyStore", e);
        }
    }
}
