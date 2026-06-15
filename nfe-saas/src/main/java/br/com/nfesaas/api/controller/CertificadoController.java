package br.com.nfesaas.api.controller;

import br.com.nfesaas.application.certificado.CertificadoService;
import br.com.nfesaas.domain.enums.TipoAmbiente;
import br.com.nfesaas.domain.model.CertificadoDigital;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * Endpoints para gerenciamento do Certificado Digital A1 da empresa.
 *
 * POST /empresas/{empresaId}/certificado
 *   Recebe o arquivo .pfx, a senha e o ambiente (PRODUCAO ou HOMOLOGACAO).
 *   O sistema valida, criptografa e armazena — o cliente nunca precisa
 *   mandar o certificado diretamente ao desenvolvedor.
 */
@RestController
@RequestMapping("/empresas/{empresaId}/certificado")
@RequiredArgsConstructor
public class CertificadoController {

    private final CertificadoService certificadoService;

    /**
     * Upload do certificado A1.
     *
     * Exemplo de chamada com curl:
     *   curl -X POST http://localhost:8080/empresas/{id}/certificado \
     *        -F "pfx=@/caminho/certificado.pfx" \
     *        -F "senha=minhaSenha123" \
     *        -F "ambiente=PRODUCAO"
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadCertificado(
            @PathVariable UUID empresaId,
            @RequestPart("pfx") MultipartFile pfxFile,
            @RequestParam("senha") String senha,
            @RequestParam(value = "ambiente", defaultValue = "PRODUCAO") TipoAmbiente ambiente) {

        CertificadoDigital cert = certificadoService.uploadCertificado(
                empresaId, pfxFile, senha, ambiente);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Certificado enviado e armazenado com sucesso",
                "validade", cert.getDataValidade().toString(),
                "numeroDeSerie", cert.getNumeroDeSerie(),
                "ambiente", cert.getAmbiente().name()
        ));
    }
}
