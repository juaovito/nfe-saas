package br.com.nfesaas.application.nfeservico;

import br.com.nfesaas.application.certificado.CertificadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

/**
 * Assina digitalmente o XML da DPS usando o certificado A1 (PKCS12) da empresa.
 *
 * Padrão utilizado: XML Digital Signature (XMLDSig) — W3C / ICP-Brasil
 *  - Algoritmo de assinatura : RSA-SHA1  (SHA1withRSA)
 *  - Canonicalização         : C14N 1.0
 *  - Transform               : Enveloped Signature + C14N 1.0
 *  - Referência              : ID do elemento InfDeclaracaoPrestacaoServico
 *
 * O XML assinado é o mesmo exigido pelo webservice ABRASF 2.01 de Maringá.
 */
@Component
@RequiredArgsConstructor
public class DpsXmlSigner {

    private final CertificadoService certificadoService;

    /**
     * Assina o XML da DPS.
     *
     * @param xmlDps    XML gerado pelo DpsXmlGenerator (sem assinatura)
     * @param empresaId ID da empresa — usado para carregar o certificado A1
     * @return XML assinado como String UTF-8
     */
    public String assinar(String xmlDps, UUID empresaId) {
        try {
            // 1. Carrega o KeyStore do certificado A1
            KeyStore ks = certificadoService.carregarKeyStore(empresaId);
            String alias = encontrarAlias(ks);
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, null);
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            // 2. Faz o parse do XML para DOM
            Document doc = parsearXml(xmlDps);

            // 3. Localiza o elemento a ser assinado (InfDeclaracaoPrestacaoServico com Id)
            Element elementoAssinado = encontrarElementoComId(doc, "InfDeclaracaoPrestacaoServico");
            String idReferencia = elementoAssinado.getAttribute("Id");

            // 4. Configura a fábrica de assinatura XMLDSig
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // 4a. Transforms: Enveloped + C14N
            List<Transform> transforms = new ArrayList<>();
            transforms.add(fac.newTransform(
                    Transform.ENVELOPED, (TransformParameterSpec) null));
            transforms.add(fac.newTransform(
                    CanonicalizationMethod.INCLUSIVE,
                    (C14NMethodParameterSpec) null));

            // 4b. Reference apontando para o Id do elemento
            Reference ref = fac.newReference(
                    "#" + idReferencia,
                    fac.newDigestMethod(DigestMethod.SHA1, null),
                    transforms,
                    null,
                    null);

            // 4c. SignedInfo com C14N e RSA-SHA1
            SignedInfo signedInfo = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(
                            CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    Collections.singletonList(ref));

            // 4d. KeyInfo com o certificado X.509
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
            KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

            // 5. Cria a assinatura e define onde ela será inserida no DOM
            //    A assinatura é inserida dentro do elemento Rps (enveloped)
            Element elementoPai = (Element) elementoAssinado.getParentNode();
            DOMSignContext dsc = new DOMSignContext(privateKey, elementoPai);

            // 6. Assina
            XMLSignature signature = fac.newXMLSignature(signedInfo, keyInfo);
            signature.sign(dsc);

            // 7. Serializa o documento assinado de volta para String
            return serializar(doc);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao assinar XML da DPS", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Encontra o alias da chave privada no KeyStore.
     * Certificados A1 geralmente têm apenas um alias, mas o loop garante
     * que encontramos o alias com chave privada mesmo quando há mais de um.
     */
    private String encontrarAlias(KeyStore ks) throws Exception {
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (ks.isKeyEntry(alias)) {
                return alias;
            }
        }
        throw new RuntimeException("Nenhuma chave privada encontrada no certificado A1");
    }

    /**
     * Faz o parse do XML String para um Document DOM com namespace-awareness.
     * Necessário para que o XMLDSig funcione corretamente.
     */
    private Document parsearXml(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Localiza o primeiro elemento com o nome dado que possua atributo "Id".
     * O XMLDSig precisa referenciar esse Id para saber o que assinar.
     */
    private Element encontrarElementoComId(Document doc, String nomeElemento) {
        NodeList nos = doc.getElementsByTagNameNS("*", nomeElemento);
        for (int i = 0; i < nos.getLength(); i++) {
            Element el = (Element) nos.item(i);
            if (el.hasAttribute("Id") && !el.getAttribute("Id").isBlank()) {
                return el;
            }
        }
        throw new RuntimeException(
                "Elemento <" + nomeElemento + " Id=\"...\"> não encontrado no XML da DPS. "
                + "Verifique se o DpsXmlGenerator está gerando o atributo Id corretamente.");
    }

    /**
     * Serializa o Document DOM para String UTF-8 sem declaração XML redundante.
     */
    private String serializar(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "no"); // ABRASF não exige indentação

        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
