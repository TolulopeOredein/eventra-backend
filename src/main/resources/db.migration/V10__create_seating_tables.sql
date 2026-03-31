-- ============================================
-- SEATING AI SCHEMA
-- ============================================

SET search_path TO seating;

CREATE TABLE seating_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tables (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seating_plan_id UUID REFERENCES seating_plans(id),
    table_number INT NOT NULL,
    capacity INT NOT NULL,
    shape VARCHAR(20),
    x_position INT,
    y_position INT,
    rotation INT,
    is_vip BOOLEAN DEFAULT false,
    zone VARCHAR(50)
);

CREATE TABLE seating_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seating_plan_id UUID REFERENCES seating_plans(id),
    table_id UUID REFERENCES tables(id),
    guest_id UUID REFERENCES guest_management.guests(id),
    seat_number INT,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE social_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    guest_id UUID REFERENCES guest_management.guests(id),
    connected_guest_id UUID REFERENCES guest_management.guests(id),
    connection_strength DECIMAL(3,2),
    connection_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_id, guest_id, connected_guest_id)
);

CREATE TABLE conflicts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    guest_id UUID REFERENCES guest_management.guests(id),
    conflict_guest_id UUID REFERENCES guest_management.guests(id),
    conflict_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);