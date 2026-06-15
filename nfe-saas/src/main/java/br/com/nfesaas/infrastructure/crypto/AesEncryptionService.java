package br.com.nfesaas.infrastructure.crypto;

import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Criptografia AES-256-GCM para proteger a senha do certificado digital.
 * O IV (12 bytes) é gerado aleatoriamente a cada cifragem e prefixado
 * no ciphertext: Base64(IV):Base64(ciphertext+tag).
 * Instanciado pelo {@link br.com.nfesaas.config.CryptoConfig}.
 */
public class AesEncryptionService {

    private static final String ALGORITHM  = "AES/GCM/NoPadding";
    private static final int    IV_LENGTH  = 12;   // bytes
    private static final int    TAG_LENGTH = 128;  // bits

    private final SecretKey secretKey;

    public AesEncryptionService(@Value("${crypto.aes-master-key}") String masterKey) {
        byte[] keyBytes = masterKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        this.secretKey = new SecretKeySpec(keyBytes, 0, 32, "AES");
    }

    /** Cifra o texto e retorna Base64(IV):Base64(ciphertext). */
    public String criptografar(String texto) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(iv)
                    + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar", e);
        }
    }

    /** Decifra o valor produzido por {@link #criptografar(String)}. */
    public String descriptografar(String textoCriptografado) {
        try {
            String[] parts = textoCriptografado.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Formato inválido: esperado Base64(IV):Base64(cipher)");
            }
            byte[] iv         = Base64.getDecoder().decode(parts[0]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] plain = cipher.doFinal(ciphertext);

            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar", e);
        }
    }
}
