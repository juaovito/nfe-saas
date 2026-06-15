package br.com.nfesaas.infrastructure.sefaz;

import lombok.Data;

@Data
public class SefazResponse {
    private String cStat;
    private String xMotivo;
    private String nProt;
    private String xmlProtocolo;
    private boolean autorizada;
}
