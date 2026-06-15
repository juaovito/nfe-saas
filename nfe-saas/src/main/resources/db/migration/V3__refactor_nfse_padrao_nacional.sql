-- ================================================================
-- NF-e SaaS — Refatoração para NFS-e Padrão Nacional (Flyway V3)
-- ================================================================

-- Ajustes na tabela empresas: regime tributário e numeração NFS-e
ALTER TABLE empresas
    CHANGE COLUMN regime regime_tributario VARCHAR(30) NOT NULL,
    CHANGE COLUMN ultimo_numero_nfe ultimo_numero_nfse BIGINT DEFAULT 0,
    DROP COLUMN serie_nfe;

-- ================================================================
-- Serviços (substitui produtos para NFS-e)
-- ================================================================
CREATE TABLE IF NOT EXISTS servicos (
    id                          VARCHAR(36)   PRIMARY KEY DEFAULT (UUID()),
    empresa_id                  VARCHAR(36)   NOT NULL,
    descricao                   VARCHAR(500)  NOT NULL,
    codigo_interno              VARCHAR(60),
    codigo_tributacao_nacional  VARCHAR(6)    NOT NULL,
    codigo_tributacao_municipal VARCHAR(20),
    item_lista_servico          VARCHAR(7),
    cnae                        VARCHAR(7),
    valor_unitario              DECIMAL(15,4) NOT NULL,
    unidade_codigo              VARCHAR(6),
    aliquota_iss                DECIMAL(5,2),
    iss_retido                  TINYINT(1)    NOT NULL DEFAULT 0,
    reducao_base_iss            DECIMAL(5,2),
    ativo                       TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em                   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_servico_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

CREATE INDEX idx_servico_empresa             ON servicos(empresa_id);
CREATE INDEX idx_servico_codigo_trib_nacional ON servicos(codigo_tributacao_nacional);

-- ================================================================
-- Notas Fiscais de Serviço (DPS / NFS-e Padrão Nacional)
-- ================================================================
CREATE TABLE IF NOT EXISTS notas_fiscais_servico (
    id                              VARCHAR(36)   PRIMARY KEY DEFAULT (UUID()),
    empresa_id                      VARCHAR(36)   NOT NULL,
    cliente_id                      VARCHAR(36)   NOT NULL,
    tipo                            VARCHAR(10)   NOT NULL,
    ambiente                        VARCHAR(15)   NOT NULL,
    status                          VARCHAR(20)   NOT NULL,

    serie_dps                       VARCHAR(5)    NOT NULL,
    numero_dps                      BIGINT        NOT NULL,
    chave_acesso_dps                VARCHAR(50),
    data_emissao                    DATETIME      NOT NULL,
    competencia                     DATE          NOT NULL,

    codigo_municipio_prestacao      VARCHAR(7)    NOT NULL,
    codigo_pais_prestacao           VARCHAR(4),

    natureza_tributacao             VARCHAR(40)   NOT NULL,
    regime_tributario                VARCHAR(30)   NOT NULL,
    optante_simples_nacional        TINYINT(1)    NOT NULL DEFAULT 0,
    regime_especial_tributacao       INT,

    chave_acesso                    VARCHAR(50)   UNIQUE,
    numero_nfse                     BIGINT,
    codigo_verificacao               VARCHAR(9),
    data_autorizacao                 DATETIME,

    valor_servicos                   DECIMAL(15,2) NOT NULL,
    valor_deducoes                   DECIMAL(15,2),
    valor_desconto_incondicionado    DECIMAL(15,2),
    valor_desconto_condicionado      DECIMAL(15,2),
    base_calculo_iss                 DECIMAL(15,2),
    valor_iss                        DECIMAL(15,2),
    valor_total_nota                 DECIMAL(15,2) NOT NULL,
    valor_pis                        DECIMAL(15,2),
    valor_cofins                     DECIMAL(15,2),
    valor_ir                         DECIMAL(15,2),
    valor_inss                       DECIMAL(15,2),
    valor_csll                       DECIMAL(15,2),

    xml_dps                          TEXT,
    xml_dps_assinado                 TEXT,
    xml_nfse                         TEXT,
    numero_protocolo                 VARCHAR(50),
    motivo_rejeicao                  TEXT,
    informacoes_complementares       TEXT,

    ativo                            TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em                        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em                    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_nfse_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_nfse_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT uq_nfse_empresa_serie_numero UNIQUE (empresa_id, serie_dps, numero_dps)
);

CREATE INDEX idx_nfse_empresa ON notas_fiscais_servico(empresa_id);
CREATE INDEX idx_nfse_status  ON notas_fiscais_servico(status);

-- ================================================================
-- Itens das Notas Fiscais de Serviço
-- ================================================================
CREATE TABLE IF NOT EXISTS itens_nota_fiscal_servico (
    id                              VARCHAR(36)   PRIMARY KEY DEFAULT (UUID()),
    nota_fiscal_servico_id          VARCHAR(36)   NOT NULL,
    servico_id                      VARCHAR(36)   NOT NULL,
    numero_item                     INT           NOT NULL,
    descricao                       VARCHAR(500)  NOT NULL,
    codigo_tributacao_nacional      VARCHAR(6)    NOT NULL,
    codigo_tributacao_municipal     VARCHAR(20),
    quantidade                      DECIMAL(15,4) NOT NULL,
    valor_unitario                  DECIMAL(15,4) NOT NULL,
    valor_servico                   DECIMAL(15,2) NOT NULL,
    valor_desconto_incondicionado   DECIMAL(15,2),
    valor_desconto_condicionado     DECIMAL(15,2),
    valor_deducoes                  DECIMAL(15,2),
    base_calculo_iss                DECIMAL(15,2),
    aliquota_iss                    DECIMAL(5,2),
    valor_iss                       DECIMAL(15,2),
    iss_retido                      TINYINT(1)    NOT NULL DEFAULT 0,
    valor_pis                       DECIMAL(15,2),
    valor_cofins                    DECIMAL(15,2),
    valor_ir                        DECIMAL(15,2),
    valor_inss                      DECIMAL(15,2),
    valor_csll                      DECIMAL(15,2),
    ativo                           TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em                       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em                   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_nfse_nota FOREIGN KEY (nota_fiscal_servico_id) REFERENCES notas_fiscais_servico(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_nfse_servico FOREIGN KEY (servico_id) REFERENCES servicos(id)
);

CREATE INDEX idx_item_nfse_nota ON itens_nota_fiscal_servico(nota_fiscal_servico_id);

-- ================================================================
-- Tabelas legadas do modelo NF-e de mercadorias (desativadas)
-- Mantidas temporariamente para não quebrar dados existentes;
-- serão removidas em uma migration futura após a migração completa
-- dos Controllers/Services para o domínio de Serviços.
-- ================================================================
-- DROP TABLE IF EXISTS itens_nota_fiscal;
-- DROP TABLE IF EXISTS notas_fiscais;
-- DROP TABLE IF EXISTS produtos;
