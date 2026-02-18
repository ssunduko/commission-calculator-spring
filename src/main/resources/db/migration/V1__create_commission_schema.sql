-- ============================================================
-- FLYWAY MIGRATION: V1 - Create Commission Calculator Schema
-- ============================================================
--
-- WHAT IS FLYWAY?
-- Flyway is a database migration tool that manages schema versioning.
-- Instead of manually running SQL scripts, Flyway automatically detects
-- and applies new migration scripts in order.
--
-- NAMING CONVENTION:
-- V{version}__{description}.sql
--   V  = Versioned migration (applied once, never modified)
--   1  = Version number (must be unique and sequential)
--   __ = Double underscore separator (REQUIRED)
--   create_commission_schema = Human-readable description
--
-- OTHER MIGRATION TYPES:
--   R__description.sql = Repeatable migration (re-run when checksum changes)
--   U{version}__description.sql = Undo migration (Flyway Teams only)
--
-- FLYWAY WORKFLOW:
-- 1. Flyway checks the 'flyway_schema_history' table
-- 2. Compares applied versions against available migration files
-- 3. Applies any new migrations in version order
-- 4. Records each migration in 'flyway_schema_history'
--
-- BEST PRACTICES:
-- - NEVER modify an already-applied migration (checksums will fail)
-- - Create a NEW migration for schema changes
-- - Keep migrations small and focused
-- - Use meaningful descriptions in filenames
-- - Test migrations against production-like data
-- ============================================================

-- ============================================================
-- TABLE: users
-- The root entity in our domain model.
-- Referenced by: deals, commission_calculations, disputes
-- ============================================================
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP,
    created_date DATE NOT NULL,
    created_by VARCHAR(255),
    -- Self-referential FK: manager is also a User
    manager_id VARCHAR(36),
    department VARCHAR(255),
    territory VARCHAR(255),
    CONSTRAINT fk_user_manager FOREIGN KEY (manager_id) REFERENCES users(id)
);

-- INDEXES: Optimize queries on frequently searched columns
CREATE INDEX idx_user_department ON users(department);
CREATE INDEX idx_user_territory ON users(territory);
CREATE INDEX idx_user_active ON users(active);

-- ============================================================
-- TABLE: user_roles
-- ElementCollection table for User.roles (Set<UserRole>)
-- This is NOT an entity table - it stores enum values for the
-- @ElementCollection mapping on User.roles
-- ============================================================
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role)
);

-- ============================================================
-- TABLE: commission_plans
-- Aggregate root for commission plan configuration.
-- Parent of: commission_rules, commission_tiers, bonus_rules
-- ============================================================
CREATE TABLE commission_plans (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    effective_start_date DATE,
    effective_end_date DATE,
    created_date DATE NOT NULL,
    last_modified_date DATE,
    created_by VARCHAR(255)
);

CREATE INDEX idx_plan_status ON commission_plans(status);
CREATE INDEX idx_plan_effective_dates ON commission_plans(effective_start_date, effective_end_date);

-- ============================================================
-- TABLE: commission_rules
-- Child of commission_plans (many rules per plan)
-- Parent of: rule_conditions
-- ============================================================
CREATE TABLE commission_rules (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    rate DECIMAL(19,2) NOT NULL DEFAULT 0,
    type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    priority INT NOT NULL DEFAULT 0,
    plan_id VARCHAR(36),
    CONSTRAINT fk_rule_plan FOREIGN KEY (plan_id) REFERENCES commission_plans(id) ON DELETE CASCADE
);

CREATE INDEX idx_rule_plan_id ON commission_rules(plan_id);
CREATE INDEX idx_rule_type ON commission_rules(type);
CREATE INDEX idx_rule_priority ON commission_rules(priority);

-- ============================================================
-- TABLE: rule_conditions
-- Child of commission_rules (many conditions per rule)
-- Leaf entity in the aggregate hierarchy
-- ============================================================
CREATE TABLE rule_conditions (
    id VARCHAR(36) PRIMARY KEY,
    field VARCHAR(255) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    condition_value VARCHAR(255) NOT NULL,
    logical_operator VARCHAR(10) NOT NULL DEFAULT 'AND',
    rule_id VARCHAR(36),
    CONSTRAINT fk_condition_rule FOREIGN KEY (rule_id) REFERENCES commission_rules(id) ON DELETE CASCADE
);

CREATE INDEX idx_rc_rule_id ON rule_conditions(rule_id);

-- ============================================================
-- TABLE: commission_tiers
-- Child of commission_plans (many tiers per plan)
-- Implements tiered rate structures (like tax brackets)
-- ============================================================
CREATE TABLE commission_tiers (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    lower_bound DECIMAL(19,2) NOT NULL DEFAULT 0,
    upper_bound DECIMAL(19,2),
    rate DECIMAL(19,2) NOT NULL DEFAULT 0,
    is_percentage BOOLEAN NOT NULL DEFAULT TRUE,
    plan_id VARCHAR(36),
    CONSTRAINT fk_tier_plan FOREIGN KEY (plan_id) REFERENCES commission_plans(id) ON DELETE CASCADE
);

CREATE INDEX idx_tier_plan_id ON commission_tiers(plan_id);

-- ============================================================
-- TABLE: bonus_rules
-- Child of commission_plans (many bonus rules per plan)
-- Defines incentive structures with time-bounded applicability
-- ============================================================
CREATE TABLE bonus_rules (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    is_percentage BOOLEAN NOT NULL DEFAULT FALSE,
    type VARCHAR(30) NOT NULL DEFAULT 'FIXED',
    start_date DATE,
    end_date DATE,
    plan_id VARCHAR(36),
    CONSTRAINT fk_bonus_plan FOREIGN KEY (plan_id) REFERENCES commission_plans(id) ON DELETE CASCADE
);

CREATE INDEX idx_br_plan_id ON bonus_rules(plan_id);
CREATE INDEX idx_br_type ON bonus_rules(type);
CREATE INDEX idx_br_dates ON bonus_rules(start_date, end_date);

-- ============================================================
-- TABLE: deals
-- Core business entity - sales deals that generate commissions
-- References: users (sales rep)
-- Parent of: deal_products, commission_calculations
-- ============================================================
CREATE TABLE deals (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    deal_value DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    sales_rep_id VARCHAR(36) NOT NULL,
    close_date DATE,
    created_date DATE NOT NULL,
    last_modified_date DATE,
    CONSTRAINT fk_deal_sales_rep FOREIGN KEY (sales_rep_id) REFERENCES users(id)
);

CREATE INDEX idx_deal_status ON deals(status);
CREATE INDEX idx_deal_sales_rep ON deals(sales_rep_id);
CREATE INDEX idx_deal_close_date ON deals(close_date);
CREATE INDEX idx_deal_created_date ON deals(created_date);

-- ============================================================
-- TABLE: deal_products
-- Child of deals (many products per deal)
-- Lifecycle managed by parent via CascadeType.ALL + orphanRemoval
-- ============================================================
CREATE TABLE deal_products (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(255),
    product_name VARCHAR(255),
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(19,2) NOT NULL,
    discount DECIMAL(19,2) DEFAULT 0,
    deal_id VARCHAR(36),
    CONSTRAINT fk_dp_deal FOREIGN KEY (deal_id) REFERENCES deals(id) ON DELETE CASCADE
);

CREATE INDEX idx_dp_deal_id ON deal_products(deal_id);

-- ============================================================
-- TABLE: commission_calculations
-- Result of applying commission rules to deals
-- References: deals, users, commission_plans
-- Parent of: bonus_calculations, accelerator_calculations
-- ============================================================
CREATE TABLE commission_calculations (
    id VARCHAR(36) PRIMARY KEY,
    deal_id VARCHAR(36) NOT NULL,
    sales_rep_id VARCHAR(36) NOT NULL,
    base_commission DECIMAL(19,2) NOT NULL DEFAULT 0,
    gross_commission DECIMAL(19,2) NOT NULL DEFAULT 0,
    net_commission DECIMAL(19,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'CALCULATED',
    calculation_date DATE NOT NULL,
    payout_date DATE,
    plan_id VARCHAR(36),
    calculated_by VARCHAR(255),
    CONSTRAINT fk_calc_deal FOREIGN KEY (deal_id) REFERENCES deals(id),
    CONSTRAINT fk_calc_sales_rep FOREIGN KEY (sales_rep_id) REFERENCES users(id),
    CONSTRAINT fk_calc_plan FOREIGN KEY (plan_id) REFERENCES commission_plans(id)
);

CREATE INDEX idx_calc_deal_id ON commission_calculations(deal_id);
CREATE INDEX idx_calc_sales_rep_id ON commission_calculations(sales_rep_id);
CREATE INDEX idx_calc_plan_id ON commission_calculations(plan_id);
CREATE INDEX idx_calc_status ON commission_calculations(status);
CREATE INDEX idx_calc_date ON commission_calculations(calculation_date);
CREATE INDEX idx_calc_payout_date ON commission_calculations(payout_date);

-- ============================================================
-- TABLE: bonus_calculations
-- Child of commission_calculations
-- Stores individual bonus amounts applied to a calculation
-- ============================================================
CREATE TABLE bonus_calculations (
    id VARCHAR(36) PRIMARY KEY,
    bonus_rule_id VARCHAR(255),
    bonus_name VARCHAR(255),
    amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    commission_calculation_id VARCHAR(36),
    description VARCHAR(500),
    CONSTRAINT fk_bc_calc FOREIGN KEY (commission_calculation_id)
        REFERENCES commission_calculations(id) ON DELETE CASCADE
);

CREATE INDEX idx_bc_calc_id ON bonus_calculations(commission_calculation_id);

-- ============================================================
-- TABLE: accelerator_calculations
-- Child of commission_calculations
-- Stores multiplier accelerators applied to a calculation
-- ============================================================
CREATE TABLE accelerator_calculations (
    id VARCHAR(36) PRIMARY KEY,
    rule_id VARCHAR(255),
    rule_name VARCHAR(255),
    multiplier DECIMAL(19,4) NOT NULL DEFAULT 1.0,
    commission_calculation_id VARCHAR(36),
    description VARCHAR(500),
    CONSTRAINT fk_ac_calc FOREIGN KEY (commission_calculation_id)
        REFERENCES commission_calculations(id) ON DELETE CASCADE
);

CREATE INDEX idx_ac_calc_id ON accelerator_calculations(commission_calculation_id);

-- ============================================================
-- TABLE: disputes
-- Commission disputes filed by sales reps
-- References: commission_calculations, users (salesRep + manager)
-- Parent of: dispute_comments
-- ============================================================
CREATE TABLE disputes (
    id VARCHAR(36) PRIMARY KEY,
    calculation_id VARCHAR(36) NOT NULL,
    sales_rep_id VARCHAR(36) NOT NULL,
    manager_id VARCHAR(36),
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'INITIATED',
    created_date TIMESTAMP NOT NULL,
    last_updated_date TIMESTAMP,
    resolved_date TIMESTAMP,
    resolved_by VARCHAR(255),
    resolution VARCHAR(2000),
    is_escalated BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_dispute_calc FOREIGN KEY (calculation_id) REFERENCES commission_calculations(id),
    CONSTRAINT fk_dispute_sales_rep FOREIGN KEY (sales_rep_id) REFERENCES users(id),
    CONSTRAINT fk_dispute_manager FOREIGN KEY (manager_id) REFERENCES users(id)
);

CREATE INDEX idx_dispute_calc_id ON disputes(calculation_id);
CREATE INDEX idx_dispute_sales_rep ON disputes(sales_rep_id);
CREATE INDEX idx_dispute_manager ON disputes(manager_id);
CREATE INDEX idx_dispute_status ON disputes(status);
CREATE INDEX idx_dispute_created ON disputes(created_date);

-- ============================================================
-- TABLE: dispute_comments
-- Child of disputes (many comments per dispute)
-- ============================================================
CREATE TABLE dispute_comments (
    id VARCHAR(36) PRIMARY KEY,
    dispute_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(255),
    user_name VARCHAR(255),
    text VARCHAR(2000) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    is_system_comment BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_dc_dispute FOREIGN KEY (dispute_id) REFERENCES disputes(id) ON DELETE CASCADE
);

CREATE INDEX idx_dc_dispute_id ON dispute_comments(dispute_id);
CREATE INDEX idx_dc_timestamp ON dispute_comments(timestamp);
