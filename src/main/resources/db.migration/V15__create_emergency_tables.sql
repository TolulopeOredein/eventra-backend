-- ============================================
-- EMERGENCY RESPONSE SCHEMA
-- ============================================

SET search_path TO emergency;

CREATE TABLE alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    reported_by UUID REFERENCES user_management.users(id),
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active',
    resolved_at TIMESTAMP,
    resolved_by UUID REFERENCES user_management.users(id),
    resolution_notes TEXT
);

CREATE TABLE responders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    responder_type VARCHAR(50) NOT NULL,
    name VARCHAR(255),
    phone VARCHAR(20),
    is_available BOOLEAN DEFAULT true,
    assigned_to UUID REFERENCES alerts(id),
    assigned_at TIMESTAMP
);

CREATE TABLE evacuation_routes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    route_name VARCHAR(255),
    assembly_point VARCHAR(255),
    capacity INT,
    instructions TEXT,
    map_url TEXT
);