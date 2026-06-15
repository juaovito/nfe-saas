-- ================================================================
-- NF-e SaaS — Schema inicial (Flyway V1) — MySQL 8.0
-- ================================================================

CREATE TABLE IF NOT EXISTS empresas (
    id                      VARCHAR(36)  PRIMARY KEY DEFAULT (UUID()),
    cnpj                    VARCHAR(14)  NOT NULL UNIQUE,
    razao_social            VARCHAR(255) NOT NULL,
    nome_fantasia           VARCHAR(255),
    inscricao_estadual      VARCHAR(15),
    inscricao_municipal     VARCHAR(15),
    regime                  VARCHAR(30)  NOT NULL,
    cep                     VARCHAR(8),
    logradouro              VARCHAR(255),
    numero                  VARCHAR(20),
    complemento             VARCHAR(100),
    bairro                  VARCHAR(100),
    municipio               VARCHAR(100),
    uf                      VARCHAR(2),
    codigo_ibge_municipio   VARCHAR(7),
    email                   VARCHAR(255),
    telefone                VARCHAR(15),
    serie_nfe               INT          DEFAULT 1,
    ultimo_numero_nfe       BIGINT       DEFAULT 0,
    ativo                   TINYINT(1)   NOT NULL DEFAULT 1,
    criado_em               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS certificados_digitais (
    id                  VARCHAR(36)  PRIMARY KEY DEFAULT (UUID()),
    empresa_id          VARCHAR(36)  NOT NULL UNIQUE,
    storage_path        VARCHAR(500) NOT NULL,
    senha_criptografada TEXT         NOT NULL,
    data_validade       DATE         NOT NULL,
    numero_de_serie     VARCHAR(100),
    cnpj_certificado    VARCHAR(14),
    ambiente            VARCHAR(15)  NOT NULL,
    ativo               TINYINT(1)   NOT NULL DEFAULT 1,
    criado_em           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cert_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

CREATE TABLE IF NOT EXISTS clientes (
    id                      VARCHAR(36)  PRIMARY KEY DEFAULT (UUID()),
    empresa_id              VARCHAR(36)  NOT NULL,
    cpf_cnpj                VARCHAR(14)  NOT NULL,
    nome                    VARCHAR(255) NOT NULL,
    inscricao_estadual      VARCHAR(15),
    pessoa_juridica         TINYINT(1)   NOT NULL,
    cep                     VARCHAR(8),
    logradouro              VARCHAR(255),
    numero                  VARCHAR(20),
    complemento             VARCHAR(100),
    bairro                  VARCHAR(100),
    municipio               VARCHAR(100),
    uf                      VARCHAR(2),
    codigo_ibge_municipio   VARCHAR(7),
    email                   VARCHAR(255),
    telefone                VARCHAR(15),
    consumidor_final        TINYINT(1)   NOT NULL DEFAULT 0,
    ativo                   TINYINT(1)   NOT NULL DEFAULT 1,
    criado_em               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

CREATE INDEX idx_cliente_empresa  ON clientes(empresa_id);
CREATE INDEX idx_cliente_cpf_cnpj ON clientes(cpf_cnpj);

CREATE TABLE IF NOT EXISTS produtos (
    id                  VARCHAR(36)   PRIMARY KEY DEFAULT (UUID()),
    empresa_id          VARCHAR(36)   NOT NULL,
    descricao           VARCHAR(500)  NOT NULL,
    codigo_interno      VARCHAR(60),
    ncm                 VARCHAR(8)    NOT NULL,
    cest                VARCHAR(7),
    cfop                VARCHAR(60)   NOT NULL,
    codigo_barras       VARCHAR(14),
    unidade_comercial   VARCHAR(6)    NOT NULL,
    cst_icms            VARCHAR(3)    NOT NULL,
    aliquota_icms       DECIMAL(6,2),
    reducao_base_icms   DECIMAL(6,2),
    cst_pis             VARCHAR(3),
    aliquota_pis        DECIMAL(6,2),
    cst_cofins          VARCHAR(3),
    aliquota_cofins     DECIMAL(6,2),
    cst_ipi             VARCHAR(3),
    aliquota_ipi        DECIMAL(6,2),
    preco_unitario      DECIMAL(15,4) NOT NULL,
    ativo               TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_produto_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

CREATE INDEX idx_produto_empresa ON produtos(empresa_id);
CREATE INDEX idx_produto_ncm     ON produtos(ncm);

CREATE TABLE IF NOT EXISTS notas_fiscais (
    id                          VARCHAR(36)   PRIMARY KEY DEFAULT (UUID()),
    empresa_id                  VARCHAR(36)   NOT NULL,
    cliente_id                  VARCHAR(36)   NOT NULL,
    numero                      BIGINT        NOT NULL,
    serie                       INT           NOT NULL,
    chave_acesso                VARCHAR(44)   UNIQUE,
    tipo                        VARCHAR(10)   NOT NULL,
    ambiente                    VARCHAR(15)   NOT NULL,
    status                      VARCHAR(20)   NOT NULL,
    data_emissao                DATETIME      NOT NULL,
    data_saida_entrada          DATETIME,
    data_autorizacao            DATETIME,
    valor_total_produtos        DECIMAL(15,2) NOT NULL,
    valor_total_nota            DECIMAL(15,2) NOT NULL,
    valor_icms                  DECIMAL(15,2),
    valor_pis                   DECIMAL(15,2),
    valor_cofins                DECIMAL(15,2),
    valor_ipi                   DECIMAL(15,2),
    valor_frete                 DECIMAL(15,2),
    valor_desconto              DECIMAL(15,2),
    xml_assinado                TEXT,
    xml_autorizado              TEXT,
    numero_protocolo            VARCHAR(50),
    motivo_rejeicao             TEXT,
    informacoes_complementares  TEXT,
    ativo                       TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em                   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_nf_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_nf_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE INDEX idx_nf_empresa ON notas_fiscais(empresa_id);
CREATE INDEX idx_nf_status  ON notas_fiscais(status);

CREATE TABLE IF NOT EXISTS itens_nota_fiscal (
    id                  VARCHAR(36)   PRIMARY KEY DEFAULT (UUID()),
    nota_fiscal_id      VARCHAR(36)   NOT NULL,
    produto_id          VARCHAR(36)   NOT NULL,
    numero_item         INT           NOT NULL,
    descricao_override  VARCHAR(500),
    quantidade          DECIMAL(15,4) NOT NULL,
    valor_unitario      DECIMAL(15,4) NOT NULL,
    valor_total         DECIMAL(15,2) NOT NULL,
    valor_desconto      DECIMAL(15,2),
    base_calculo_icms   DECIMAL(15,2),
    valor_icms          DECIMAL(15,2),
    valor_pis           DECIMAL(15,2),
    valor_cofins        DECIMAL(15,2),
    valor_ipi           DECIMAL(15,2),
    ncm                 VARCHAR(8)    NOT NULL,
    cfop                VARCHAR(60)   NOT NULL,
    cst_icms            VARCHAR(3)    NOT NULL,
    ativo               TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_nota FOREIGN KEY (nota_fiscal_id) REFERENCES notas_fiscais(id) ON DELETE CASCADE
);