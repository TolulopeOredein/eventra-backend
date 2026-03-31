-- ============================================
-- GUEST COMMUNICATION SCHEMA
-- ============================================

SET search_path TO guest_communication;

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    guest_id UUID REFERENCES guest_management.guests(id),
    message_type VARCHAR(20) NOT NULL,
    subject VARCHAR(255),
    content TEXT NOT NULL,
    media_url TEXT[],
    recipient_type VARCHAR(20),
    recipient_id UUID,
    show_on_gift_wall BOOLEAN DEFAULT false,
    show_guest_name BOOLEAN DEFAULT true,
    status VARCHAR(20) DEFAULT 'pending',
    read_at TIMESTAMP,
    responded_at TIMESTAMP,
    response_content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE help_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    guest_id UUID REFERENCES guest_management.guests(id),
    request_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) DEFAULT 'normal',
    description TEXT NOT NULL,
    media_url TEXT[],
    table_number INT,
    guest_description TEXT,
    venue_section VARCHAR(100),
    assigned_to UUID REFERENCES user_management.users(id),
    assigned_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'pending',
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    guest_rating INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE guest_identifiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    guest_id UUID REFERENCES guest_management.guests(id),
    table_number INT,
    gender VARCHAR(10),
    outfit_color VARCHAR(50),
    outfit_type VARCHAR(100),
    distinctive_features TEXT,
    notes TEXT
);

CREATE TABLE guest_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    guest_id UUID REFERENCES guest_management.guests(id),
    feedback_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    rating INT,
    category VARCHAR(50),
    responded BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);