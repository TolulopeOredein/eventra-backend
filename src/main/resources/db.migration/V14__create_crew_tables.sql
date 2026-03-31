-- ============================================
-- CREW MANAGEMENT SCHEMA
-- ============================================

SET search_path TO crew_management;

CREATE TABLE crew_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_vendor_id UUID REFERENCES vendor_management.event_vendors(id),
    vendor_id UUID REFERENCES vendor_management.vendors(id),
    name VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    photo_url TEXT,
    government_id TEXT,
    certifications TEXT[],
    qr_code_url TEXT,
    qr_code_scanned BOOLEAN DEFAULT false,
    scan_time TIMESTAMP,
    attendance_status VARCHAR(20) DEFAULT 'scheduled',
    hours_worked DECIMAL(5,2),
    overtime_hours DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_member_id UUID REFERENCES crew_members(id),
    task_name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    priority VARCHAR(20) DEFAULT 'normal',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    proof_photo_url TEXT,
    notes TEXT
);

CREATE TABLE equipment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_vendor_id UUID REFERENCES vendor_management.event_vendors(id),
    equipment_name VARCHAR(255) NOT NULL,
    equipment_type VARCHAR(50),
    quantity INT DEFAULT 1,
    serial_numbers TEXT[],
    condition VARCHAR(50),
    checked_in BOOLEAN DEFAULT false,
    checked_in_time TIMESTAMP,
    checked_out BOOLEAN DEFAULT false,
    checked_out_time TIMESTAMP
);

CREATE TABLE crew_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_member_id UUID REFERENCES crew_members(id),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    accuracy DECIMAL(10,2),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);