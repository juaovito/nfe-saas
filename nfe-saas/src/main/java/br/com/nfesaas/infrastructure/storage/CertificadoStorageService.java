package br.com.nfesaas.infrastructure.storage;

import org.springframework.stereotype.Service;

/**
 * Stub — Armazena e recupera o arquivo .pfx criptografado.
 * MVP: filesystem local. Produção: AWS S3 + KMS ou HashiCorp Vault.
 */
@Service
public class CertificadoStorageService {
    // TODO: salvar(UUID empresaId, byte[] pfxCriptografado) -> String storagePath
    // TODO: carregar(String storagePath) -> byte[] pfxCriptografado
}
