-- ============================================
-- GUEST LIFECYCLE SCHEMA
-- ============================================

SET search_path TO guest_lifecycle;

CREATE TABLE welcome_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    message_template TEXT NOT NULL,
    include_table_info BOOLEAN DEFAULT true,
    include_vip_info BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE thank_you_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    immediate_message TEXT,
    photo_gallery_message TEXT,
    anniversary_message TEXT,
    include_gallery_link BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE guest_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guest_id UUID REFERENCES guest_management.guests(id),
    event_id UUID REFERENCES event_management.events(id),
    event_type VARCHAR(50) NOT NULL,
    triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message_sent BOOLEAN DEFAULT false,
    channel VARCHAR(20)
);