-- =============================================================================
-- WarrantyHub — Relational Database Schema
-- Phase 2: Database Design & Supabase Setup
-- Application: Product Warranty Registration Portal
-- Database: PostgreSQL 15+ / Supabase
-- Target Engine: Java 17 / Spring Boot 3.2.x with Spring Data JPA
-- =============================================================================

-- Enable pgcrypto extension for gen_random_uuid() (standard for PostgreSQL)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- AUTOMATED TIMESTAMP TRIGGER FUNCTION
-- =============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- 1. USERS TABLE
-- =============================================================================
-- Stores authentication credentials, legal name, role authorization, and auditing timestamps.
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('CUSTOMER', 'ADMIN')),
    CONSTRAINT chk_users_email_not_empty CHECK (LENGTH(TRIM(email)) > 0),
    CONSTRAINT chk_users_name_not_empty CHECK (LENGTH(TRIM(name)) > 0)
);

-- Users Indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Trigger for users updated_at
DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- 2. PRODUCTS TABLE
-- =============================================================================
-- Stores customer-registered products with purchase metadata and warranty terms.
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model_number VARCHAR(100) NOT NULL,
    serial_number VARCHAR(150) NOT NULL,
    purchase_date DATE NOT NULL,
    seller_name VARCHAR(255) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    warranty_duration_months INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_products_price CHECK (price >= 0),
    CONSTRAINT chk_products_warranty_duration CHECK (warranty_duration_months > 0),
    CONSTRAINT chk_products_name_not_empty CHECK (LENGTH(TRIM(product_name)) > 0),
    CONSTRAINT chk_products_serial_not_empty CHECK (LENGTH(TRIM(serial_number)) > 0)
);

-- Products Indexes
CREATE INDEX IF NOT EXISTS idx_products_user_id ON products(user_id);
CREATE INDEX IF NOT EXISTS idx_products_serial_number ON products(serial_number);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand);

-- Trigger for products updated_at
DROP TRIGGER IF EXISTS trg_products_updated_at ON products;
CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- 3. WARRANTIES TABLE
-- =============================================================================
-- Stores 1-to-1 warranty tracking records automatically calculated from product purchases.
CREATE TABLE IF NOT EXISTS warranties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_warranties_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_warranties_date_order CHECK (expiry_date >= start_date),
    CONSTRAINT chk_warranties_status CHECK (status IN ('ACTIVE', 'EXPIRING_SOON', 'EXPIRED'))
);

-- Warranties Indexes
CREATE INDEX IF NOT EXISTS idx_warranties_product_id ON warranties(product_id);
CREATE INDEX IF NOT EXISTS idx_warranties_status ON warranties(status);
CREATE INDEX IF NOT EXISTS idx_warranties_expiry_date ON warranties(expiry_date);

-- Trigger for warranties updated_at
DROP TRIGGER IF EXISTS trg_warranties_updated_at ON warranties;
CREATE TRIGGER trg_warranties_updated_at
    BEFORE UPDATE ON warranties
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- 4. INVOICES TABLE
-- =============================================================================
-- Stores metadata of uploaded proof-of-purchase documents stored in Supabase Storage.
CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL UNIQUE,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoices_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT chk_invoices_file_size CHECK (file_size > 0 AND file_size <= 10485760),
    CONSTRAINT chk_invoices_file_type CHECK (file_type IN ('application/pdf', 'image/jpeg', 'image/jpg', 'image/png'))
);

-- Invoices Indexes
CREATE INDEX IF NOT EXISTS idx_invoices_user_id ON invoices(user_id);
CREATE INDEX IF NOT EXISTS idx_invoices_product_id ON invoices(product_id);
CREATE INDEX IF NOT EXISTS idx_invoices_storage_path ON invoices(storage_path);

-- =============================================================================
-- 5. CLAIMS TABLE
-- =============================================================================
-- Stores warranty claims initiated by customers for evaluation and adjudication.
CREATE TABLE IF NOT EXISTS claims (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    issue_description TEXT NOT NULL,
    additional_information TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_claims_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_claims_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT chk_claims_issue_not_empty CHECK (LENGTH(TRIM(issue_description)) > 0),
    CONSTRAINT chk_claims_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- Claims Indexes
CREATE INDEX IF NOT EXISTS idx_claims_user_id ON claims(user_id);
CREATE INDEX IF NOT EXISTS idx_claims_product_id ON claims(product_id);
CREATE INDEX IF NOT EXISTS idx_claims_status ON claims(status);

-- Trigger for claims updated_at
DROP TRIGGER IF EXISTS trg_claims_updated_at ON claims;
CREATE TRIGGER trg_claims_updated_at
    BEFORE UPDATE ON claims
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- ROW LEVEL SECURITY (RLS) DEFENSE-IN-DEPTH
-- =============================================================================
-- Row Level Security is enabled across all application tables.
-- The Spring Boot backend connects via JDBC using the 'postgres' role which possesses
-- BYPASSRLS capability, ensuring full operational access.
-- Direct PostgREST client queries via anon/authenticated keys are blocked by default.
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE warranties ENABLE ROW LEVEL SECURITY;
ALTER TABLE invoices ENABLE ROW LEVEL SECURITY;
ALTER TABLE claims ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- SUPABASE STORAGE BUCKET CONFIGURATION (REFERENCE)
-- =============================================================================
-- Storage bucket 'invoices' created as private with 10MB file limit:
-- INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
-- VALUES (
--     'invoices',
--     'invoices',
--     false,
--     10485760,
--     ARRAY['application/pdf', 'image/jpeg', 'image/png']
-- )
-- ON CONFLICT (id) DO NOTHING;
