-- ============================================
-- EVENT MANAGEMENT SCHEMA
-- ============================================

SET search_path TO event_management;

CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    venue VARCHAR(255),
    venue_address TEXT,
    city VARCHAR(100),
    state VARCHAR(50),
    country VARCHAR(50) DEFAULT 'Nigeria',
    event_date TIMESTAMP NOT NULL,
    dress_code VARCHAR(100),
    expected_guests INT,
    event_type VARCHAR(50),
    event_style VARCHAR(50),
    created_by UUID REFERENCES user_management.users(id),
    beneficiary_id UUID REFERENCES user_management.users(id),
    beneficiary_verified BOOLEAN DEFAULT false,
    beneficiary_verified_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'draft',
    settings JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_events_created_by ON events(created_by);
CREATE INDEX idx_events_beneficiary ON events(beneficiary_id);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_date ON events(event_date);

CREATE TABLE event_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(50),
    description TEXT,
    default_settings JSONB,
    icon_url TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO event_types (name, slug, category) VALUES
('Wedding', 'wedding', 'social'),
('Church Service', 'church', 'religious'),
('Corporate Event', 'corporate', 'business'),
('Burial/Memorial', 'burial', 'solemn'),
('Birthday Party', 'birthday', 'social'),
('Concert', 'concert', 'entertainment'),
('Political Rally', 'political', 'political');