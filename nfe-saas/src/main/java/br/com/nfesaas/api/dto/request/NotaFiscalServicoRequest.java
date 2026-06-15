package br.com.nfesaas.api.dto.request;

import br.com.nfesaas.domain.enums.NaturezaTributacao;
import br.com.nfesaas.domain.enums.TipoAmbiente;
import br.com.nfesaas.domain.enums.TipoNota;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class NotaFiscalServicoRequest {

    @NotNull public UUID clienteId;

    @NotNull public TipoNota tipo;

    @NotNull public TipoAmbiente ambiente;

    /** Competência (data de referência da prestação do serviço). */
    @NotNull public LocalDate competencia;

    /**
     * Código IBGE (7 dígitos) do município onde o serviço foi prestado.
     * Se não informado, assume o município da empresa emitente.
     */
    @Size(min = 7, max = 7)
    public String codigoMunicipioPrestacao;

    /** Obrigatório apenas quando a prestação ocorre no exterior. */
    public String codigoPaisPrestacao;

    @NotNull public NaturezaTributacao naturezaTributacao;

    /** Código do regime especial de tributação (tabela ABRASF/ADN), se houver. */
    public Integer regimeEspecialTributacao;

    public String informacoesComplementares;

    @NotEmpty @Valid public List<ItemServicoRequest> itens;
}
