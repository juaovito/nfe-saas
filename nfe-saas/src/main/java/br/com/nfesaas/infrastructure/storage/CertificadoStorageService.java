package br.com.nfesaas.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Armazena e recupera o arquivo .pfx criptografado no filesystem local.
 *
 * MVP: diretório local configurável via propriedade certificados.storage-path.
 * Produção: substituir por AWS S3 + KMS ou HashiCorp Vault.
 *
 * O arquivo salvo já chega criptografado (responsabilidade do CertificadoService),
 * então não há dado sensível em texto puro no disco.
 */
@Service
public class CertificadoStorageService {

    private final Path baseDir;

    public CertificadoStorageService(
            @Value("${certificados.storage-path:./certificados}") String storagePath) {
        this.baseDir = Paths.get(storagePath);
    }

    /**
     * Salva os bytes do .pfx criptografado e retorna o path relativo.
     *
     * @param empresaId ID da empresa dona do certificado
     * @param pfxBytes  bytes do .pfx já criptografados com AES-256-GCM
     * @return path relativo usado para recuperar o arquivo depois
     */
    public String salvar(UUID empresaId, byte[] pfxBytes) {
        try {
            Files.createDirectories(baseDir);
            String nomeArquivo = empresaId.toString() + ".pfx.enc";
            Path destino = baseDir.resolve(nomeArquivo);
            Files.write(destino, pfxBytes);
            return nomeArquivo;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar certificado no storage", e);
        }
    }

    /**
     * Carrega os bytes do .pfx criptografado a partir do path salvo anteriormente.
     *
     * @param storagePath caminho relativo retornado pelo método {@link #salvar}
     * @return bytes do .pfx criptografado
     */
    public byte[] carregar(String storagePath) {
        try {
            Path arquivo = baseDir.resolve(storagePath);
            if (!Files.exists(arquivo)) {
                throw new RuntimeException("Certificado não encontrado: " + storagePath);
            }
            return Files.readAllBytes(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar certificado do storage", e);
        }
    }
}
