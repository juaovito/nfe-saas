package br.com.nfesaas.application.nfeservico;

import br.com.nfesaas.domain.enums.NaturezaTributacao;
import br.com.nfesaas.domain.enums.RegimeTributario;
import br.com.nfesaas.domain.model.Cliente;
import br.com.nfesaas.domain.model.Empresa;
import br.com.nfesaas.domain.model.ItemServicoNota;
import br.com.nfesaas.domain.model.NotaFiscalServico;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Gera o XML da DPS (Declaração de Prestação de Serviços)
 * no padrão nacional NFS-e — ABRASF 2.01 / ADN (Ambiente de Dados Nacional).
 *
 * O XML gerado ainda não está assinado — a assinatura digital com o
 * certificado A1 é feita pela classe DpsXmlSigner (Parte 2).
 *
 * Namespace utilizado: http://www.abrasf.org.br/nfse.xsd
 */
@Component
public class DpsXmlGenerator {

    private static final String NS = "http://www.abrasf.org.br/nfse.xsd";
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Gera o XML da DPS sem assinatura.
     *
     * @param nota    NotaFiscalServico com todos os dados preenchidos
     * @param empresa Empresa prestadora do serviço
     * @param cliente Tomador do serviço
     * @return String com o XML completo da DPS
     */
    public String gerar(NotaFiscalServico nota, Empresa empresa, Cliente cliente) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            // Raiz: EnviarLoteRpsEnvio
            Element envio = doc.createElementNS(NS, "EnviarLoteRpsEnvio");
            envio.setAttribute("xmlns", NS);
            doc.appendChild(envio);

            // LoteRps
            Element loteRps = el(doc, "LoteRps");
            loteRps.setAttribute("Id", "Lote_" + nota.getNumeroDps());
            envio.appendChild(loteRps);

            texto(doc, loteRps, "NumeroLote", String.valueOf(nota.getNumeroDps()));
            texto(doc, loteRps, "CpfCnpj", limparMascara(empresa.getCnpj()));
            texto(doc, loteRps, "InscricaoMunicipal", empresa.getInscricaoMunicipal());
            texto(doc, loteRps, "QuantidadeRps", "1");

            // ListaRps
            Element listaRps = el(doc, "ListaRps");
            loteRps.appendChild(listaRps);

            // Rps
            Element rps = el(doc, "Rps");
            rps.setAttribute("Id", "Rps_" + nota.getNumeroDps());
            listaRps.appendChild(rps);

            // ---- InfDeclaracaoPrestacaoServico ----
            Element infDps = el(doc, "InfDeclaracaoPrestacaoServico");
            infDps.setAttribute("Id", "DPS_" + nota.getNumeroDps());
            rps.appendChild(infDps);

            // Rps (identificação)
            Element rpsId = el(doc, "Rps");
            infDps.appendChild(rpsId);
            Element identRps = el(doc, "IdentificacaoRps");
            rpsId.appendChild(identRps);
            texto(doc, identRps, "Numero", String.valueOf(nota.getNumeroDps()));
            texto(doc, identRps, "Serie", nota.getSerieDps());
            texto(doc, identRps, "Tipo", "1"); // 1 = RPS

            texto(doc, rpsId, "DataEmissao",
                    nota.getDataEmissao().format(DT_FMT));
            texto(doc, rpsId, "NaturezaOperacao",
                    naturezaParaCodigo(nota.getNaturezaTributacao()));
            texto(doc, rpsId, "OptanteSimplesNacional",
                    nota.isOptanteSimplesNacional() ? "1" : "2");
            texto(doc, rpsId, "IncentivadorCultural", "2"); // 2 = não

            if (nota.getRegimeEspecialTributacao() != null) {
                texto(doc, rpsId, "RegimeEspecialTributacao",
                        String.valueOf(nota.getRegimeEspecialTributacao()));
            }

            texto(doc, rpsId, "Status", "1"); // 1 = Normal

            // ---- Competência ----
            texto(doc, infDps, "Competencia",
                    nota.getCompetencia().format(DATE_FMT));

            // ---- Prestador ----
            Element prestador = el(doc, "Prestador");
            infDps.appendChild(prestador);
            Element cpfCnpjPrestador = el(doc, "CpfCnpj");
            prestador.appendChild(cpfCnpjPrestador);
            texto(doc, cpfCnpjPrestador, "Cnpj", limparMascara(empresa.getCnpj()));
            texto(doc, prestador, "InscricaoMunicipal", empresa.getInscricaoMunicipal());

            // ---- Tomador ----
            Element tomador = el(doc, "Tomador");
            infDps.appendChild(tomador);

            Element identTomador = el(doc, "IdentificacaoTomador");
            tomador.appendChild(identTomador);
            Element cpfCnpjTomador = el(doc, "CpfCnpj");
            identTomador.appendChild(cpfCnpjTomador);

            String docTomador = limparMascara(cliente.getCpfCnpj());
            if (cliente.isPessoaJuridica()) {
                texto(doc, cpfCnpjTomador, "Cnpj", docTomador);
            } else {
                texto(doc, cpfCnpjTomador, "Cpf", docTomador);
            }

            texto(doc, tomador, "RazaoSocial", cliente.getNome());

            // Endereço do tomador
            if (cliente.getLogradouro() != null) {
                Element endTomador = el(doc, "Endereco");
                tomador.appendChild(endTomador);
                textoSeNaoNulo(doc, endTomador, "Endereco", cliente.getLogradouro());
                textoSeNaoNulo(doc, endTomador, "Numero", cliente.getNumero());
                textoSeNaoNulo(doc, endTomador, "Complemento", cliente.getComplemento());
                textoSeNaoNulo(doc, endTomador, "Bairro", cliente.getBairro());
                textoSeNaoNulo(doc, endTomador, "CodigoMunicipio",
                        cliente.getCodigoIbgeMunicipio());
                textoSeNaoNulo(doc, endTomador, "Uf", cliente.getUf());
                textoSeNaoNulo(doc, endTomador, "CodigoPais", "1058"); // Brasil
                textoSeNaoNulo(doc, endTomador, "Cep",
                        limparMascara(cliente.getCep()));
            }

            textoSeNaoNulo(doc, tomador, "Contato", cliente.getEmail());

            // ---- Serviços ----
            Element servicos = el(doc, "Servico");
            infDps.appendChild(servicos);

            // Itens
            Element valores = el(doc, "Valores");
            servicos.appendChild(valores);
            texto(doc, valores, "ValorServicos",
                    fmt(nota.getValorServicos()));
            textoSeNaoNulo(doc, valores, "ValorDeducoes",
                    fmtOp(nota.getValorDeducoes()));
            textoSeNaoNulo(doc, valores, "ValorPis",
                    fmtOp(nota.getValorPis()));
            textoSeNaoNulo(doc, valores, "ValorCofins",
                    fmtOp(nota.getValorCofins()));
            textoSeNaoNulo(doc, valores, "ValorInss",
                    fmtOp(nota.getValorInss()));
            textoSeNaoNulo(doc, valores, "ValorIr",
                    fmtOp(nota.getValorIr()));
            textoSeNaoNulo(doc, valores, "ValorCsll",
                    fmtOp(nota.getValorCsll()));
            texto(doc, valores, "IssRetido",
                    issRetidoNota(nota) ? "1" : "2");
            textoSeNaoNulo(doc, valores, "ValorIss",
                    fmtOp(nota.getValorIss()));
            textoSeNaoNulo(doc, valores, "ValorIssRetido",
                    issRetidoNota(nota) ? fmtOp(nota.getValorIss()) : null);
            textoSeNaoNulo(doc, valores, "OutrasRetencoes", null);
            textoSeNaoNulo(doc, valores, "BaseCalculo",
                    fmtOp(nota.getBaseCalculoIss()));
            textoSeNaoNulo(doc, valores, "Aliquota",
                    aliquotaMedia(nota));
            texto(doc, valores, "ValorLiquidoNfse",
                    fmt(nota.getValorTotalNota()));
            textoSeNaoNulo(doc, valores, "DescontoIncondicionado",
                    fmtOp(nota.getValorDescontoIncondicionado()));
            textoSeNaoNulo(doc, valores, "DescontoCondicionado",
                    fmtOp(nota.getValorDescontoCondicionado()));

            // Código do serviço (primeiro item)
            if (!nota.getItens().isEmpty()) {
                ItemServicoNota primeiro = nota.getItens().get(0);
                texto(doc, servicos, "ItemListaServico",
                        primeiro.getCodigoTributacaoNacional());
                textoSeNaoNulo(doc, servicos, "CodigoCnae", null);
                textoSeNaoNulo(doc, servicos, "CodigoTributacaoMunicipio",
                        primeiro.getCodigoTributacaoMunicipal());

                // Discriminação: concatena descrições de todos os itens
                StringBuilder disc = new StringBuilder();
                for (ItemServicoNota item : nota.getItens()) {
                    if (disc.length() > 0) disc.append(" | ");
                    disc.append(item.getDescricao());
                }
                if (nota.getInformacoesComplementares() != null) {
                    disc.append(" | ").append(nota.getInformacoesComplementares());
                }
                texto(doc, servicos, "Discriminacao", disc.toString());
            }

            texto(doc, servicos, "CodigoMunicipio",
                    nota.getCodigoMunicipioPrestacao());
            texto(doc, servicos, "CodigoPaisPrestacaoServico",
                    nota.getCodigoPaisPrestacao() != null
                            ? nota.getCodigoPaisPrestacao() : "1058");
            texto(doc, servicos, "ExigibilidadeISS",
                    exigibilidadeIss(nota.getNaturezaTributacao()));
            textoSeNaoNulo(doc, servicos, "MunicipioIncidencia",
                    nota.getCodigoMunicipioPrestacao());

            // ---- Serialização ----
            return serializar(doc);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar XML da DPS", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers de construção do DOM
    // -------------------------------------------------------------------------

    private Element el(Document doc, String tag) {
        return doc.createElementNS(NS, tag);
    }

    private void texto(Document doc, Element pai, String tag, String valor) {
        Element el = doc.createElementNS(NS, tag);
        el.setTextContent(valor);
        pai.appendChild(el);
    }

    private void textoSeNaoNulo(Document doc, Element pai, String tag, String valor) {
        if (valor != null && !valor.isBlank()) {
            texto(doc, pai, tag, valor);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers de conversão
    // -------------------------------------------------------------------------

    private String limparMascara(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("[^0-9]", "");
    }

    private String fmt(BigDecimal valor) {
        if (valor == null) return "0.00";
        return valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String fmtOp(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) return null;
        return fmt(valor);
    }

    /** Converte o enum NaturezaTributacao para o código numérico da ABRASF. */
    private String naturezaParaCodigo(NaturezaTributacao nat) {
        return switch (nat) {
            case TRIBUTACAO_MUNICIPIO_PRESTADOR         -> "1";
            case TRIBUTACAO_MUNICIPIO_TOMADOR           -> "2";
            case TRIBUTACAO_EXTERIOR                    -> "3";
            case ISENTO                                 -> "4";
            case IMUNE                                  -> "5";
            case SUSPENSA_DECISAO_JUDICIAL              -> "6";
            case SUSPENSA_PROCEDIMENTO_ADMINISTRATIVO  -> "7";
        };
    }

    /** Exigibilidade do ISS mapeada a partir da natureza de tributação. */
    private String exigibilidadeIss(NaturezaTributacao nat) {
        return switch (nat) {
            case TRIBUTACAO_MUNICIPIO_PRESTADOR,
                 TRIBUTACAO_MUNICIPIO_TOMADOR   -> "1"; // Exigível
            case ISENTO                         -> "2"; // Não incidência
            case IMUNE                          -> "4"; // Imune
            case SUSPENSA_DECISAO_JUDICIAL      -> "5"; // Suspensa - decisão judicial
            case SUSPENSA_PROCEDIMENTO_ADMINISTRATIVO -> "6"; // Suspensa - proc. admin.
            case TRIBUTACAO_EXTERIOR            -> "3"; // Exportação
        };
    }

    /** Verifica se algum item da nota tem ISS retido. */
    private boolean issRetidoNota(NotaFiscalServico nota) {
        return nota.getItens().stream().anyMatch(ItemServicoNota::isIssRetido);
    }

    /** Calcula alíquota média ponderada para o campo Aliquota da nota. */
    private String aliquotaMedia(NotaFiscalServico nota) {
        if (nota.getBaseCalculoIss() == null
                || nota.getBaseCalculoIss().compareTo(BigDecimal.ZERO) == 0
                || nota.getValorIss() == null) {
            return null;
        }
        BigDecimal aliquota = nota.getValorIss()
                .divide(nota.getBaseCalculoIss(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return aliquota.setScale(4, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    // -------------------------------------------------------------------------
    // Serialização
    // -------------------------------------------------------------------------

    private String serializar(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
