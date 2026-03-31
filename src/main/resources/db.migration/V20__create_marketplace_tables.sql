-- ============================================
-- MARKETPLACE & API SCHEMA
-- ============================================

SET search_path TO marketplace;

CREATE TABLE api_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES user_management.users(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    api_secret_hash TEXT NOT NULL,
    app_type VARCHAR(20) DEFAULT 'third_party',
    allowed_ips TEXT[],
    webhook_url VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE api_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID REFERENCES api_applications(id),
    endpoint VARCHAR(255),
    method VARCHAR(10),
    status_code INT,
    response_time_ms INT,
    ip_address VARCHAR(45),
    request_payload JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE webhook_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID REFERENCES api_applications(id),
    event_type VARCHAR(100),
    url VARCHAR(500) NOT NULL,
    secret VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE webhook_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID REFERENCES webhook_subscriptions(id),
    event_type VARCHAR(100),
    payload JSONB,
    response_status INT,
    delivered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);