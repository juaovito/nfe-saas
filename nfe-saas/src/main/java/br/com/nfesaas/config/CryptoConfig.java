package br.com.nfesaas.config;

import br.com.nfesaas.infrastructure.crypto.AesEncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expõe o {@link AesEncryptionService} como bean Spring,
 * injetando a chave AES master via propriedade de ambiente.
 */
@Configuration
public class CryptoConfig {

    @Bean
    public AesEncryptionService aesEncryptionService(
            @Value("${crypto.aes-master-key}") String masterKey) {
        return new AesEncryptionService(masterKey);
    }
}
