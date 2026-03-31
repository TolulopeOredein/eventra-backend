-- ============================================
-- PAYMENT & ESCROW SCHEMA
-- ============================================

SET search_path TO payment_escrow;

CREATE TABLE payment_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    provider_key VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    api_base_url VARCHAR(255),
    secret_key_encrypted TEXT,
    public_key VARCHAR(255),
    webhook_secret_encrypted TEXT,
    supports_cards BOOLEAN DEFAULT true,
    supports_bank_transfer BOOLEAN DEFAULT true,
    supports_ussd BOOLEAN DEFAULT false,
    transaction_fee_percentage DECIMAL(5,2),
    transaction_fee_fixed DECIMAL(10,2),
    settlement_days INT DEFAULT 2,
    priority INT DEFAULT 1,
    supported_currencies TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference VARCHAR(255) UNIQUE NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    platform_fee DECIMAL(15,2) DEFAULT 0,
    net_amount DECIMAL(15,2) NOT NULL,
    payer_id UUID REFERENCES user_management.users(id),
    payee_id UUID REFERENCES user_management.users(id),
    event_id UUID REFERENCES event_management.events(id),
    payment_method VARCHAR(50),
    provider_id UUID REFERENCES payment_providers(id),
    provider_reference VARCHAR(255),
    status VARCHAR(20) DEFAULT 'pending',
    initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    is_escrow BOOLEAN DEFAULT false,
    escrow_release_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE escrow_wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID REFERENCES transactions(id) UNIQUE,
    transaction_type VARCHAR(50) NOT NULL,
    payer_id UUID REFERENCES user_management.users(id),
    payee_id UUID REFERENCES user_management.users(id),
    event_id UUID REFERENCES event_management.events(id),
    total_amount DECIMAL(15,2) NOT NULL,
    platform_fee DECIMAL(15,2) NOT NULL,
    net_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    hold_started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scheduled_release_at TIMESTAMP,
    released_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID REFERENCES transactions(id),
    escrow_wallet_id UUID REFERENCES escrow_wallets(id),
    initiated_by UUID REFERENCES user_management.users(id),
    reason VARCHAR(100) NOT NULL,
    description TEXT,
    evidence_urls TEXT[],
    status VARCHAR(20) DEFAULT 'open',
    resolution VARCHAR(100),
    resolved_by UUID REFERENCES user_management.users(id),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE withdrawal_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES user_management.users(id),
    amount DECIMAL(15,2) NOT NULL,
    bank_code VARCHAR(10),
    account_number VARCHAR(20),
    account_name VARCHAR(255),
    status VARCHAR(20) DEFAULT 'pending',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE settlement_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID REFERENCES transactions(id),
    recipient_id UUID REFERENCES user_management.users(id),
    amount DECIMAL(15,2),
    bank_code VARCHAR(10),
    account_number VARCHAR(20),
    account_name VARCHAR(255),
    status VARCHAR(20) DEFAULT 'pending',
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_kyc (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES user_management.users(id) UNIQUE,
    verification_level INT DEFAULT 0,
    phone_verified BOOLEAN DEFAULT false,
    email_verified BOOLEAN DEFAULT false,
    payment_provider_verified BOOLEAN DEFAULT false,
    bank_account_verified BOOLEAN DEFAULT false,
    bank_account_name VARCHAR(255),
    bank_name VARCHAR(100),
    bank_account_number VARCHAR(20),
    bank_code VARCHAR(10),
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);