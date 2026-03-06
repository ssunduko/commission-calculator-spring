-- ============================================================
-- FLYWAY MIGRATION: V4 - Add Companies, User Profiles, and Join Table
-- ============================================================
-- Adds tables for the Company entity, UserProfile entity,
-- and the user_companies many-to-many join table.
-- ============================================================

-- ============================================================
-- TABLE: companies
-- Represents client companies that users work with.
-- ============================================================
CREATE TABLE companies (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    industry VARCHAR(255),
    website VARCHAR(255),
    description VARCHAR(2000),
    created_date DATE NOT NULL
);

CREATE INDEX idx_company_name ON companies(name);
CREATE INDEX idx_company_industry ON companies(industry);

-- ============================================================
-- TABLE: user_companies
-- Join table for the @ManyToMany relationship between
-- Company and User. A user can work with many companies,
-- and a company can have many users.
-- ============================================================
CREATE TABLE user_companies (
    company_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    CONSTRAINT pk_user_companies PRIMARY KEY (company_id, user_id),
    CONSTRAINT fk_uc_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_uc_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ============================================================
-- TABLE: user_profiles
-- One-to-one extension of the users table for optional
-- profile information (bio, phone, LinkedIn, etc.).
-- ============================================================
CREATE TABLE user_profiles (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    bio VARCHAR(2000),
    phone_number VARCHAR(255),
    linkedin_url VARCHAR(255),
    profile_picture_url VARCHAR(255),
    job_title VARCHAR(255),
    location VARCHAR(255),
    CONSTRAINT fk_up_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE UNIQUE INDEX idx_user_profile_user_id ON user_profiles(user_id);
