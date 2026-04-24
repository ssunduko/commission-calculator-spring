-- ============================================================
-- FLYWAY MIGRATION: V10 - Subscription, Package, and Payment Tables
-- ============================================================
-- Schema for the self-service registration + subscription flow.
-- Users pick a subscription_package at signup, a subscription row is
-- created for them, and a payments row captures the card charge.
-- ============================================================

CREATE TABLE subscription_packages (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    monthly_price DECIMAL(10, 2) NOT NULL,
    max_users INT NOT NULL,
    max_deals_per_month INT NOT NULL,
    tier VARCHAR(20) NOT NULL DEFAULT 'BASIC',
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_subscription_package_active ON subscription_packages(active);
CREATE INDEX idx_subscription_package_tier ON subscription_packages(tier);

CREATE TABLE subscriptions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    package_id VARCHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE,
    next_billing_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_subscription_package FOREIGN KEY (package_id) REFERENCES subscription_packages(id)
);

CREATE INDEX idx_subscription_user ON subscriptions(user_id);
CREATE INDEX idx_subscription_status ON subscriptions(status);

CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    subscription_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    card_holder_name VARCHAR(255) NOT NULL,
    card_last_four VARCHAR(4) NOT NULL,
    card_brand VARCHAR(50),
    transaction_reference VARCHAR(100),
    failure_reason VARCHAR(500),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(id),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_payment_subscription ON payments(subscription_id);
CREATE INDEX idx_payment_user ON payments(user_id);
CREATE INDEX idx_payment_status ON payments(status);
