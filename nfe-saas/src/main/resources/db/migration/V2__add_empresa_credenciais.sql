CREATE TABLE empresa_credenciais (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    CONSTRAINT fk_credencial_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);
