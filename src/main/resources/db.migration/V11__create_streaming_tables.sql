-- ============================================
-- LIVE STREAMING SCHEMA
-- ============================================

SET search_path TO streaming;

CREATE TABLE streams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    stream_key VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'scheduled',
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    viewer_count INT DEFAULT 0,
    recording_url TEXT,
    multi_camera BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE camera_feeds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stream_id UUID REFERENCES streams(id),
    camera_name VARCHAR(100),
    camera_url TEXT,
    is_active BOOLEAN DEFAULT true,
    display_order INT DEFAULT 0
);

CREATE TABLE stream_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stream_id UUID REFERENCES streams(id),
    user_id UUID REFERENCES user_management.users(id),
    interaction_type VARCHAR(20),
    content JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE virtual_gifts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stream_id UUID REFERENCES streams(id),
    sender_id UUID REFERENCES user_management.users(id),
    recipient_id UUID REFERENCES user_management.users(id),
    amount DECIMAL(12,2),
    currency VARCHAR(3),
    animation_type VARCHAR(50),
    message TEXT,
    displayed_on_screen BOOLEAN DEFAULT false,
    displayed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE watch_parties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stream_id UUID REFERENCES streams(id),
    host_id UUID REFERENCES user_management.users(id),
    location VARCHAR(255),
    capacity INT,
    attendees JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);