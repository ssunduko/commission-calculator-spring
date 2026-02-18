-- ============================================================
-- FLYWAY MIGRATION: V3 - Add Audit Columns
-- ============================================================
--
-- INCREMENTAL SCHEMA EVOLUTION:
-- This migration demonstrates how Flyway handles schema changes
-- AFTER the initial schema is deployed. Instead of modifying V1,
-- we create a new migration that alters existing tables.
--
-- WHY NOT MODIFY V1?
-- Once V1 is applied to any environment (dev, staging, production),
-- its checksum is recorded. Modifying it would cause Flyway to fail
-- with a checksum mismatch error. Always create new migrations.
--
-- ALTER TABLE BEST PRACTICES:
-- - Add columns with DEFAULT values to avoid NULL issues on existing rows
-- - Add new columns as nullable if existing data can't provide values
-- - Never drop columns in the same migration that adds new ones
-- - Consider the impact on existing queries and indexes
-- ============================================================

-- Add version column for optimistic locking support
-- @Version in JPA uses this column to detect concurrent modifications
ALTER TABLE deals ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE commission_plans ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE commission_calculations ADD COLUMN version BIGINT DEFAULT 0;

-- Add audit trail columns for tracking who modified records
ALTER TABLE deals ADD COLUMN last_modified_by VARCHAR(255);
ALTER TABLE commission_calculations ADD COLUMN approved_by VARCHAR(255);
ALTER TABLE commission_calculations ADD COLUMN approved_date DATE;

-- Create a view for reporting - demonstrates database views with Flyway
-- Views are useful for complex queries that are reused across the application
CREATE VIEW v_commission_summary AS
SELECT
    cc.id AS calculation_id,
    d.title AS deal_title,
    d.deal_value,
    u.first_name || ' ' || u.last_name AS sales_rep_name,
    u.department,
    u.territory,
    cc.base_commission,
    cc.gross_commission,
    cc.net_commission,
    cc.status AS calculation_status,
    cc.calculation_date,
    cp.name AS plan_name
FROM commission_calculations cc
JOIN deals d ON cc.deal_id = d.id
JOIN users u ON cc.sales_rep_id = u.id
LEFT JOIN commission_plans cp ON cc.plan_id = cp.id;
