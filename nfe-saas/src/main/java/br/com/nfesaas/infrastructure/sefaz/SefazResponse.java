package br.com.nfesaas.infrastructure.sefaz;

import lombok.Data;

/**
 * Representa a resposta do webservice ABRASF 2.01 da prefeitura.
 *
 * Campos mapeados do XML de retorno:
 *  - cStat      : código de status (100 = autorizada, 2xx = rejeição, etc.)
 *  - xMotivo    : descrição textual do status
 *  - nProt      : número do protocolo de autorização
 *  - chNFe      : chave de acesso da NFS-e gerada
 *  - nNFe       : número da NFS-e gerado pelo município
 *  - xmlRetorno : XML completo de retorno (para armazenar no banco)
 *  - autorizada : true quando cStat == "100"
 */
@Data
public class SefazResponse {

    private String cStat;
    private String xMotivo;
    private String nProt;
    private String chNFe;
    private String nNFe;
    private String xmlRetorno;
    private boolean autorizada;

    /** Constrói uma resposta de erro local (sem chegar no webservice). */
    public static SefazResponse erroLocal(String motivo) {
        SefazResponse r = new SefazResponse();
        r.setCStat("999");
        r.setXMotivo(motivo);
        r.setAutorizada(false);
        return r;
    }
}
