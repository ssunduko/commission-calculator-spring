-- ============================================================
-- V9: Add priority column to disputes
-- ============================================================
-- Mirrors DisputeStatus: enum stored as VARCHAR, default MEDIUM.
-- Existing rows get MEDIUM to match the frontend's prior hardcoded value.
-- ============================================================

ALTER TABLE disputes
    ADD COLUMN priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM';

CREATE INDEX idx_disputes_priority ON disputes(priority);
