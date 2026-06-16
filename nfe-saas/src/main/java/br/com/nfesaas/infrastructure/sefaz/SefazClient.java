package br.com.nfesaas.infrastructure.sefaz;

import br.com.nfesaas.domain.enums.TipoAmbiente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente SOAP para o webservice NFS-e ABRASF 2.01 da Prefeitura de Maringá.
 *
 * URLs:
 *  - Produção    : https://maringa.fintel.com.br/nfse/services/NfseService
 *  - Homologação : https://nfse.hom-ecity.maringa.pr.gov.br/nfse/services/NfseService
 *
 * Operação utilizada: RecepcionarLoteRps
 * O XML enviado deve estar assinado digitalmente (produzido pelo DpsXmlSigner).
 */
@Component
public class SefazClient {

    private static final String URL_PRODUCAO =
            "https://maringa.fintel.com.br/nfse/services/NfseService";

    private static final String URL_HOMOLOGACAO =
            "https://nfse.hom-ecity.maringa.pr.gov.br/nfse/services/NfseService";

    private static final String SOAP_ACTION =
            "http://www.abrasf.org.br/nfse.xsd/RecepcionarLoteRps";

    private static final int TIMEOUT_MS = 30_000; // 30 segundos

    @Value("${nfse.maringa.usuario:}")
    private String usuarioPortal;

    @Value("${nfse.maringa.senha:}")
    private String senhaPortal;

    /**
     * Envia o lote de RPS assinado para o webservice da prefeitura.
     *
     * @param xmlAssinado XML da DPS já assinado digitalmente
     * @param ambiente    PRODUCAO ou HOMOLOGACAO
     * @return SefazResponse com o resultado do processamento
     */
    public SefazResponse enviarLote(String xmlAssinado, TipoAmbiente ambiente) {
        try {
            String urlDestino = ambiente == TipoAmbiente.PRODUCAO
                    ? URL_PRODUCAO : URL_HOMOLOGACAO;

            String envelope = montarEnvelopeSoap(xmlAssinado);
            String xmlRetorno = chamarWebservice(urlDestino, envelope);
            return interpretarRetorno(xmlRetorno);

        } catch (Exception e) {
            return SefazResponse.erroLocal("Erro na comunicação com a prefeitura: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Montagem do envelope SOAP
    // -------------------------------------------------------------------------

    /**
     * Monta o envelope SOAP com o XML da DPS assinado dentro.
     * Padrão: SOAP 1.1 + namespace ABRASF.
     */
    private String montarEnvelopeSoap(String xmlDpsAssinado) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope
                    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                    xmlns:nfse="http://www.abrasf.org.br/nfse.xsd">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <nfse:RecepcionarLoteRpsRequest>
                      <nfse:xml>
                """ + xmlDpsAssinado + """
                      </nfse:xml>
                    </nfse:RecepcionarLoteRpsRequest>
                  </soapenv:Body>
                </soapenv:Envelope>
                """;
    }

    // -------------------------------------------------------------------------
    // Comunicação HTTP
    // -------------------------------------------------------------------------

    /**
     * Realiza a chamada HTTP POST ao webservice SOAP.
     * Usa HttpURLConnection puro para evitar dependência de frameworks SOAP
     * e manter compatibilidade máxima com os diferentes ambientes.
     */
    private String chamarWebservice(String urlDestino, String envelope) throws Exception {
        URL url = new URL(urlDestino);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
        conn.setRequestProperty("SOAPAction", SOAP_ACTION);
        conn.setRequestProperty("Accept", "text/xml");

        // Envia o envelope
        byte[] payload = envelope.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(payload.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
            os.flush();
        }

        // Lê a resposta (em caso de erro HTTP, lê o error stream)
        int httpStatus = conn.getResponseCode();
        var stream = httpStatus >= 400 ? conn.getErrorStream() : conn.getInputStream();

        if (stream == null) {
            throw new RuntimeException("Webservice retornou HTTP " + httpStatus + " sem corpo de resposta");
        }

        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // Interpretação do retorno
    // -------------------------------------------------------------------------

    /**
     * Interpreta o XML de retorno do webservice e popula o SefazResponse.
     *
     * O webservice ABRASF retorna um envelope SOAP com a estrutura:
     *
     * <RetornoEnviarLoteRpsResponse>
     *   <RetornoEnviarLoteRps>
     *     <Protocolo>...</Protocolo>      <- número do protocolo
     *     <Situacao>...</Situacao>        <- código de status
     *     <ListaNfse>
     *       <CompNfse>
     *         <Nfse>
     *           <InfNfse>
     *             <Numero>...</Numero>
     *             <ChaveAcesso>...</ChaveAcesso>
     *           </InfNfse>
     *         </Nfse>
     *       </CompNfse>
     *     </ListaNfse>
     *     <ListaMensagemRetorno>
     *       <MensagemRetorno>
     *         <Codigo>...</Codigo>
     *         <Mensagem>...</Mensagem>
     *       </MensagemRetorno>
     *     </ListaMensagemRetorno>
     *   </RetornoEnviarLoteRps>
     * </RetornoEnviarLoteRpsResponse>
     */
    private SefazResponse interpretarRetorno(String xmlRetorno) {
        SefazResponse response = new SefazResponse();
        response.setXmlRetorno(xmlRetorno);

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false); // ignora namespaces para simplificar o parse
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(
                    xmlRetorno.getBytes(StandardCharsets.UTF_8)));

            // Protocolo
            String protocolo = textoNo(doc, "Protocolo");
            if (protocolo != null) response.setNProt(protocolo);

            // Situação / código de status
            String situacao = textoNo(doc, "Situacao");
            if (situacao == null) situacao = textoNo(doc, "Codigo");
            response.setCStat(situacao != null ? situacao : "999");

            // Mensagem de retorno
            String mensagem = textoNo(doc, "Mensagem");
            if (mensagem == null) mensagem = textoNo(doc, "xMotivo");
            response.setXMotivo(mensagem != null ? mensagem : "Sem descrição retornada");

            // Número e chave da NFS-e (quando autorizada)
            String numero = textoNo(doc, "Numero");
            if (numero != null) response.setNNFe(numero);

            String chave = textoNo(doc, "ChaveAcesso");
            if (chave != null) response.setChNFe(chave);

            // Considera autorizada quando tem protocolo e número da NFS-e
            boolean autorizada = protocolo != null && !protocolo.isBlank()
                    && numero != null && !numero.isBlank();
            response.setAutorizada(autorizada);

            if (!autorizada && (situacao == null || situacao.equals("999"))) {
                response.setCStat("401");
                if (response.getXMotivo() == null || response.getXMotivo().isBlank()) {
                    response.setXMotivo("Nota não autorizada — verifique os dados e tente novamente");
                }
            }

        } catch (Exception e) {
            response.setCStat("999");
            response.setXMotivo("Erro ao interpretar retorno da prefeitura: " + e.getMessage());
            response.setAutorizada(false);
        }

        return response;
    }

    // -------------------------------------------------------------------------
    // Helper DOM
    // -------------------------------------------------------------------------

    /** Retorna o texto do primeiro elemento com o tag dado, ou null se não existir. */
    private String textoNo(Document doc, String tag) {
        NodeList nos = doc.getElementsByTagName(tag);
        if (nos.getLength() > 0) {
            String texto = nos.item(0).getTextContent();
            return texto != null && !texto.isBlank() ? texto.trim() : null;
        }
        return null;
    }
}
