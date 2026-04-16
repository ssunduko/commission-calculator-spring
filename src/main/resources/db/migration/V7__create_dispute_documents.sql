-- ============================================================
-- TABLE: dispute_documents
-- Child of disputes (many documents per dispute)
-- Metadata-only: no file bytes stored; backend records name/size/type
-- ============================================================
CREATE TABLE dispute_documents (
    id VARCHAR(36) PRIMARY KEY,
    dispute_id VARCHAR(36) NOT NULL,
    name VARCHAR(512) NOT NULL,
    content_type VARCHAR(255),
    size_bytes BIGINT NOT NULL,
    uploaded_by VARCHAR(255),
    uploaded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_dd_dispute FOREIGN KEY (dispute_id) REFERENCES disputes(id) ON DELETE CASCADE
);

CREATE INDEX idx_dd_dispute_id ON dispute_documents(dispute_id);
CREATE INDEX idx_dd_uploaded_at ON dispute_documents(uploaded_at);
