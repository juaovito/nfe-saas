-- Adiciona coluna de controle de concorrência otimista (@Version do JPA)
-- Previne race condition no incremento de ultimo_numero_nfse
ALTER TABLE empresas
    ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
