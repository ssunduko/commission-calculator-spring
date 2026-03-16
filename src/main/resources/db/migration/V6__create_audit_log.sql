-- ============================================================
-- FLYWAY MIGRATION: V6 - Create Audit Log Table
-- ============================================================
--
-- CONCEPT: Audit Log for Orthogonal Architecture
--
-- The audit_log table records every command (state-changing operation)
-- that passes through the orthogonal pipeline. Unlike the event_store
-- (which records domain events), the audit_log records OPERATIONS:
-- what command was executed, by which handler, and whether it succeeded.
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_log (
    id             VARCHAR(36)  PRIMARY KEY,
    operation      VARCHAR(100) NOT NULL,
    handler_name   VARCHAR(100) NOT NULL,
    input_data     TEXT,
    status         VARCHAR(20)  NOT NULL,
    error_message  VARCHAR(500),
    duration_ms    BIGINT       NOT NULL,
    occurred_at    TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_log_operation ON audit_log(operation);
CREATE INDEX IF NOT EXISTS idx_audit_log_status ON audit_log(status);
CREATE INDEX IF NOT EXISTS idx_audit_log_occurred_at ON audit_log(occurred_at);
