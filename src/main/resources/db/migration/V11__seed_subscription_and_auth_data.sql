-- ============================================================
-- FLYWAY MIGRATION: V11 - Seed Subscription Packages, Subscriptions,
--                        and Updated User Login Credentials
-- ============================================================
-- Seeds the self-service onboarding data:
--   * Three subscription tiers (BASIC, PROFESSIONAL, ENTERPRISE)
--   * BCrypt password hashes so existing seed users can log in through
--     the new /api/auth/login endpoint (V2 users had placeholder hashes)
--   * Extra demo users so the UI can show several accounts out of the box
--   * Active subscriptions for each seed user so the dashboard is populated
--
-- BCrypt hashes were generated with the 2a scheme / work factor 10.
-- Password reference table (all for local/demo use only):
--   jsmith    / sales123
--   ajones    / sales123
--   bwilson   / sales123
--   mgarcia   / manager123
--   admin     / admin123
--   demo      / demo1234
-- ============================================================

-- 1. Seed subscription packages -------------------------------------------------
INSERT INTO subscription_packages (id, code, name, description, monthly_price, max_users, max_deals_per_month, tier, active) VALUES
    ('pkg-basic',
     'BASIC',
     'Starter',
     'Perfect for individual sales reps getting started with commission tracking. Includes core calculator and dashboard.',
     19.00, 1, 50, 'BASIC', TRUE),
    ('pkg-pro',
     'PROFESSIONAL',
     'Professional',
     'Full-featured plan for sales teams. Advanced analytics, dispute management, plan builder, and priority email support.',
     79.00, 10, 500, 'PROFESSIONAL', TRUE),
    ('pkg-enterprise',
     'ENTERPRISE',
     'Enterprise',
     'Unlimited scale with A2A agents, custom integrations, single sign-on, and a dedicated customer success manager.',
     249.00, 100, 5000, 'ENTERPRISE', TRUE);

-- 2. Refresh existing V2 seed users with real BCrypt hashes --------------------
-- The {bcrypt} prefix tells our DelegatingPasswordEncoder which algorithm to
-- use when verifying the stored hash. Without it, matches() throws because
-- the encoder cannot identify the format.
UPDATE users
SET password_hash = '{bcrypt}$2a$10$L8TqZome1hLBX3z7KfUq4eDOXKg4Y0yTffovq8CmShPOcYHxWUhXK'
WHERE id IN ('usr-001', 'usr-002', 'usr-003');

UPDATE users
SET password_hash = '{bcrypt}$2a$10$22hf0AEcdvlaV72RZuOM8e0E9jOAk4x8I.4goSLqX57YQ0StsMK.e'
WHERE id = 'usr-004';

-- 3. Seed two additional demo accounts ------------------------------------------
-- The department and role of the demo user are intentionally NOT "Sales" /
-- SALES_REP so that the ORM UserRepositoryTest / UserDealIntegrationTest
-- counts (expecting three SALES_REP users in the Sales department) still hold.
INSERT INTO users (id, username, email, first_name, last_name, password_hash, active, created_date, department, territory) VALUES
    ('usr-005', 'admin', 'admin@commission-hub.io', 'System', 'Administrator',
     '{bcrypt}$2a$10$w6bwzRx1Vu1BRLWVHSoOI.o38sG7hDy1HpqM9x/kHURJYeAwp3m6G',
     TRUE, '2024-01-01', 'Platform', 'Global'),
    ('usr-006', 'demo', 'demo@commission-hub.io', 'Demo', 'User',
     '{bcrypt}$2a$10$hcp9Z.o0uUOANfTALG5HdOgO5.Sc9JoZGGTXAl7ehu/lcmeqx3qyS',
     TRUE, '2024-06-15', 'Demo', 'Demo Territory');

INSERT INTO user_roles (user_id, role) VALUES
    ('usr-005', 'SYSTEM_ADMIN'),
    ('usr-005', 'FINANCE_ADMIN'),
    ('usr-006', 'SALES_MANAGER');

-- 4. Attach a subscription to each seed user ------------------------------------
INSERT INTO subscriptions (id, user_id, package_id, status, start_date, next_billing_date, created_at) VALUES
    ('sub-001', 'usr-001', 'pkg-pro',        'ACTIVE', '2024-01-15', '2026-05-15', CURRENT_TIMESTAMP),
    ('sub-002', 'usr-002', 'pkg-pro',        'ACTIVE', '2024-01-15', '2026-05-15', CURRENT_TIMESTAMP),
    ('sub-003', 'usr-003', 'pkg-basic',      'ACTIVE', '2024-02-01', '2026-05-01', CURRENT_TIMESTAMP),
    ('sub-004', 'usr-004', 'pkg-enterprise', 'ACTIVE', '2024-01-10', '2026-05-10', CURRENT_TIMESTAMP),
    ('sub-005', 'usr-005', 'pkg-enterprise', 'ACTIVE', '2024-01-01', '2026-05-01', CURRENT_TIMESTAMP),
    ('sub-006', 'usr-006', 'pkg-basic',      'ACTIVE', '2024-06-15', '2026-05-15', CURRENT_TIMESTAMP);

-- 5. Record a completed signup payment for each subscription --------------------
INSERT INTO payments (id, subscription_id, user_id, amount, currency, status, card_holder_name, card_last_four, card_brand, transaction_reference, processed_at, created_at) VALUES
    ('pay-001', 'sub-001', 'usr-001',  79.00, 'USD', 'COMPLETED', 'John Smith',    '4242', 'VISA',       'TXN-SEED000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('pay-002', 'sub-002', 'usr-002',  79.00, 'USD', 'COMPLETED', 'Alice Jones',   '5555', 'MASTERCARD', 'TXN-SEED000000002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('pay-003', 'sub-003', 'usr-003',  19.00, 'USD', 'COMPLETED', 'Bob Wilson',    '4111', 'VISA',       'TXN-SEED000000003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('pay-004', 'sub-004', 'usr-004', 249.00, 'USD', 'COMPLETED', 'Maria Garcia',  '0005', 'AMEX',       'TXN-SEED000000004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('pay-005', 'sub-005', 'usr-005', 249.00, 'USD', 'COMPLETED', 'Admin User',    '1111', 'VISA',       'TXN-SEED000000005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('pay-006', 'sub-006', 'usr-006',  19.00, 'USD', 'COMPLETED', 'Demo User',     '2222', 'MASTERCARD', 'TXN-SEED000000006', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
