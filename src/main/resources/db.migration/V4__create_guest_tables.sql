-- ============================================
-- GUEST MANAGEMENT SCHEMA
-- ============================================

SET search_path TO guest_management;

CREATE TABLE guests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    phone_normalized VARCHAR(20),
    tier VARCHAR(20) DEFAULT 'regular',
    rsvp_status VARCHAR(20) DEFAULT 'pending',
    rsvp_at TIMESTAMP,
    meal_preference VARCHAR(100),
    dietary_restrictions TEXT,
    asobi_paid BOOLEAN DEFAULT false,
    asobi_size VARCHAR(10),
    check_in_status BOOLEAN DEFAULT false,
    check_in_time TIMESTAMP,
    check_in_gate VARCHAR(10),
    qr_code_url TEXT,
    invite_token VARCHAR(255) UNIQUE,
    invite_sent BOOLEAN DEFAULT false,
    invite_sent_at TIMESTAMP,
    invite_opened_at TIMESTAMP,
    invite_click_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_guests_event_id ON guests(event_id);
CREATE INDEX idx_guests_phone ON guests(phone);
CREATE INDEX idx_guests_rsvp_status ON guests(event_id, rsvp_status);
CREATE INDEX idx_guests_invite_token ON guests(invite_token);

CREATE TABLE invite_campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    name VARCHAR(255),
    message_template TEXT,
    recipient_count INT,
    sent_at TIMESTAMP,
    delivered_count INT,
    opened_count INT,
    rsvp_count INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);