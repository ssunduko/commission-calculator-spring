-- ============================================================
-- Hibernate's unidirectional @OneToMany @JoinColumn pattern
-- (parent manages FK, child column is insertable=false)
-- INSERTs child rows with NULL dispute_id, then UPDATEs.
-- Drop the NOT NULL so the initial INSERT doesn't fail; the FK
-- constraint still guarantees referential integrity once set.
-- ============================================================
ALTER TABLE dispute_comments ALTER COLUMN dispute_id DROP NOT NULL;
ALTER TABLE dispute_documents ALTER COLUMN dispute_id DROP NOT NULL;
