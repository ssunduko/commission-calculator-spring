-- ============================================================
-- FLYWAY MIGRATION: V2 - Seed Sample Data
-- ============================================================
--
-- DATA MIGRATION vs. SCHEMA MIGRATION:
-- V1 was a schema migration (DDL: CREATE TABLE, ALTER TABLE).
-- V2 is a data migration (DML: INSERT, UPDATE, DELETE).
--
-- Separating schema and data migrations is a best practice:
-- - Schema migrations change structure
-- - Data migrations populate or transform data
-- - Each can be reviewed and tested independently
--
-- SEED DATA:
-- Initial data required for the application to function.
-- This includes reference data, default configurations,
-- and sample data for development/testing.
-- ============================================================

-- ============================================================
-- SEED: Users (Sales team hierarchy)
-- ============================================================
INSERT INTO users (id, username, email, first_name, last_name, password_hash, active, created_date, department, territory)
VALUES
    ('usr-001', 'jsmith', 'john.smith@chapman.edu', 'John', 'Smith',
     '$2a$10$placeholder', TRUE, '2024-01-15', 'Sales', 'West Coast'),
    ('usr-002', 'ajones', 'alice.jones@chapman.edu', 'Alice', 'Jones',
     '$2a$10$placeholder', TRUE, '2024-01-15', 'Sales', 'East Coast'),
    ('usr-003', 'bwilson', 'bob.wilson@chapman.edu', 'Bob', 'Wilson',
     '$2a$10$placeholder', TRUE, '2024-02-01', 'Sales', 'Central'),
    ('usr-004', 'mgarcia', 'maria.garcia@chapman.edu', 'Maria', 'Garcia',
     '$2a$10$placeholder', TRUE, '2024-01-10', 'Sales Management', 'National');

-- Set Maria as manager for the sales reps
UPDATE users SET manager_id = 'usr-004' WHERE id IN ('usr-001', 'usr-002', 'usr-003');

-- Assign roles
INSERT INTO user_roles (user_id, role) VALUES
    ('usr-001', 'SALES_REP'),
    ('usr-002', 'SALES_REP'),
    ('usr-003', 'SALES_REP'),
    ('usr-004', 'SALES_MANAGER'),
    ('usr-004', 'FINANCE_ADMIN');

-- ============================================================
-- SEED: Commission Plans
-- ============================================================
INSERT INTO commission_plans (id, name, currency, status, effective_start_date, effective_end_date, created_date, created_by)
VALUES
    ('plan-001', 'Standard Sales Plan 2024', 'USD', 'ACTIVE', '2024-01-01', '2024-12-31', '2024-01-01', 'admin'),
    ('plan-002', 'Enterprise Sales Plan 2024', 'USD', 'ACTIVE', '2024-01-01', '2024-12-31', '2024-01-01', 'admin'),
    ('plan-003', 'Q1 Accelerator Plan', 'USD', 'INACTIVE', '2024-01-01', '2024-03-31', '2024-01-01', 'admin');

-- ============================================================
-- SEED: Commission Rules
-- ============================================================
INSERT INTO commission_rules (id, name, description, rate, type, priority, plan_id)
VALUES
    ('rule-001', 'Base Commission', 'Standard 10% commission on all deals', 10.00, 'STANDARD', 1, 'plan-001'),
    ('rule-002', 'Enterprise Accelerator', '15% rate for enterprise deals over $50K', 15.00, 'ACCELERATOR', 2, 'plan-002'),
    ('rule-003', 'Small Deal Rate', '5% rate for deals under $10K', 5.00, 'STANDARD', 1, 'plan-001');

-- ============================================================
-- SEED: Rule Conditions
-- ============================================================
INSERT INTO rule_conditions (id, field, operator, condition_value, logical_operator, rule_id)
VALUES
    ('cond-001', 'deal.value', 'GREATER_THAN_OR_EQUALS', '0', 'AND', 'rule-001'),
    ('cond-002', 'deal.value', 'GREATER_THAN_OR_EQUALS', '50000', 'AND', 'rule-002'),
    ('cond-003', 'deal.value', 'LESS_THAN', '10000', 'AND', 'rule-003');

-- ============================================================
-- SEED: Commission Tiers (tiered rate structure)
-- ============================================================
INSERT INTO commission_tiers (id, name, lower_bound, upper_bound, rate, is_percentage, plan_id)
VALUES
    ('tier-001', 'Bronze', 0.00, 25000.00, 5.00, TRUE, 'plan-001'),
    ('tier-002', 'Silver', 25000.00, 75000.00, 8.00, TRUE, 'plan-001'),
    ('tier-003', 'Gold', 75000.00, 150000.00, 12.00, TRUE, 'plan-001'),
    ('tier-004', 'Platinum', 150000.00, NULL, 15.00, TRUE, 'plan-001');

-- ============================================================
-- SEED: Bonus Rules
-- ============================================================
INSERT INTO bonus_rules (id, name, description, amount, is_percentage, type, start_date, end_date, plan_id)
VALUES
    ('bonus-001', 'New Client Bonus', 'Fixed $500 bonus for new client acquisitions', 500.00, FALSE, 'FIXED', '2024-01-01', '2024-12-31', 'plan-001'),
    ('bonus-002', 'Quarterly SPIF', '20% bonus on base for Q1', 20.00, TRUE, 'SPIF', '2024-01-01', '2024-03-31', 'plan-001'),
    ('bonus-003', 'Team Performance', '10% bonus when team exceeds quota', 10.00, TRUE, 'TEAM_PERFORMANCE', '2024-01-01', '2024-12-31', 'plan-002');

-- ============================================================
-- SEED: Deals
-- ============================================================
INSERT INTO deals (id, title, deal_value, status, sales_rep_id, close_date, created_date, last_modified_date)
VALUES
    ('deal-001', 'Acme Corp ERP Implementation', 85000.00, 'WON', 'usr-001', '2024-03-15', '2024-01-20', '2024-03-15'),
    ('deal-002', 'TechStart Cloud Migration', 32000.00, 'WON', 'usr-001', '2024-02-28', '2024-01-25', '2024-02-28'),
    ('deal-003', 'Global Retail POS System', 120000.00, 'WON', 'usr-002', '2024-04-10', '2024-02-15', '2024-04-10'),
    ('deal-004', 'StartupXYZ SaaS License', 8500.00, 'WON', 'usr-003', '2024-03-01', '2024-02-20', '2024-03-01'),
    ('deal-005', 'MegaCorp Data Platform', 250000.00, 'OPEN', 'usr-002', NULL, '2024-05-01', '2024-05-01'),
    ('deal-006', 'SmallBiz CRM Setup', 5000.00, 'LOST', 'usr-003', NULL, '2024-03-10', '2024-04-15');

-- ============================================================
-- SEED: Deal Products
-- ============================================================
INSERT INTO deal_products (id, product_id, product_name, quantity, price, discount, deal_id)
VALUES
    ('dp-001', 'PROD-ERP', 'ERP License', 50, 1200.00, 0.00, 'deal-001'),
    ('dp-002', 'PROD-IMPL', 'Implementation Services', 1, 25000.00, 0.00, 'deal-001'),
    ('dp-003', 'PROD-CLOUD', 'Cloud Migration Package', 1, 32000.00, 0.00, 'deal-002'),
    ('dp-004', 'PROD-POS', 'POS Terminal', 200, 500.00, 0.00, 'deal-003'),
    ('dp-005', 'PROD-POS-SW', 'POS Software License', 200, 100.00, 0.00, 'deal-003'),
    ('dp-006', 'PROD-SAAS', 'SaaS Annual License', 10, 850.00, 0.00, 'deal-004');

-- ============================================================
-- SEED: Commission Calculations
-- ============================================================
INSERT INTO commission_calculations (id, deal_id, sales_rep_id, base_commission, gross_commission, net_commission, status, calculation_date, plan_id, calculated_by)
VALUES
    ('calc-001', 'deal-001', 'usr-001', 8500.00, 9000.00, 9000.00, 'APPROVED', '2024-03-16', 'plan-001', 'system'),
    ('calc-002', 'deal-002', 'usr-001', 3200.00, 3200.00, 3200.00, 'PAID', '2024-03-01', 'plan-001', 'system'),
    ('calc-003', 'deal-003', 'usr-002', 14400.00, 14400.00, 14400.00, 'CALCULATED', '2024-04-11', 'plan-002', 'system'),
    ('calc-004', 'deal-004', 'usr-003', 425.00, 925.00, 925.00, 'APPROVED', '2024-03-02', 'plan-001', 'system');

-- ============================================================
-- SEED: Bonus Calculations
-- ============================================================
INSERT INTO bonus_calculations (id, bonus_rule_id, bonus_name, amount, commission_calculation_id, description)
VALUES
    ('bc-001', 'bonus-001', 'New Client Bonus', 500.00, 'calc-001', 'New client acquisition bonus for Acme Corp'),
    ('bc-002', 'bonus-001', 'New Client Bonus', 500.00, 'calc-004', 'New client acquisition bonus for StartupXYZ');

-- ============================================================
-- SEED: Disputes
-- ============================================================
INSERT INTO disputes (id, calculation_id, sales_rep_id, manager_id, title, description, status, created_date, last_updated_date, is_escalated)
VALUES
    ('disp-001', 'calc-003', 'usr-002', 'usr-004', 'Incorrect Commission Rate',
     'The commission was calculated at 12% but the enterprise plan specifies 15% for deals over $100K.',
     'UNDER_REVIEW', TIMESTAMP '2024-04-12 09:30:00', TIMESTAMP '2024-04-12 14:00:00', FALSE);

INSERT INTO dispute_comments (id, dispute_id, user_id, user_name, text, timestamp, is_system_comment)
VALUES
    ('dc-001', 'disp-001', 'usr-002', 'Alice Jones',
     'My deal for Global Retail was $120K which qualifies for the Enterprise rate of 15%, but I received only 12%.',
     TIMESTAMP '2024-04-12 09:30:00', FALSE),
    ('dc-002', 'disp-001', NULL, 'System',
     'Dispute assigned to manager Maria Garcia for review.',
     TIMESTAMP '2024-04-12 09:31:00', TRUE);
