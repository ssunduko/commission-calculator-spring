-- ============================================================
-- FLYWAY MIGRATION: V5 - Create Event Store Table
-- ============================================================
--
-- CONCEPT: Event Store Schema
--
-- The event_store table is an append-only log that records every
-- domain event. Unlike regular tables where rows are updated/deleted,
-- event store rows are ONLY inserted — events are immutable facts.
--
-- This table enables:
-- - Complete audit trail of all business actions
-- - Event replay for debugging or state reconstruction
-- - Temporal queries ("what happened between dates X and Y?")
-- ============================================================

CREATE TABLE IF NOT EXISTS event_store (
    event_id       VARCHAR(36)  PRIMARY KEY,
    event_type     VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    payload        TEXT,
    occurred_at    TIMESTAMP    NOT NULL,

    -- Indexes for common query patterns
    -- Events are frequently queried by aggregate (e.g., "all events for deal-001")
    -- and by type (e.g., "all DealCreatedEvents")
    CONSTRAINT idx_event_aggregate UNIQUE (event_id)
);

CREATE INDEX IF NOT EXISTS idx_event_store_aggregate_id ON event_store(aggregate_id);
CREATE INDEX IF NOT EXISTS idx_event_store_aggregate_type ON event_store(aggregate_type);
CREATE INDEX IF NOT EXISTS idx_event_store_event_type ON event_store(event_type);
CREATE INDEX IF NOT EXISTS idx_event_store_occurred_at ON event_store(occurred_at);
