-- ============================================
-- ENTRY MANAGEMENT SCHEMA
-- ============================================

SET search_path TO entry_management;

CREATE TABLE qr_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guest_id UUID REFERENCES guest_management.guests(id),
    event_id UUID REFERENCES event_management.events(id),
    code_hash VARCHAR(255) UNIQUE NOT NULL,
    encrypted_payload TEXT,
    revoked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    used_at TIMESTAMP
);

CREATE INDEX idx_qr_codes_guest ON qr_codes(guest_id);
CREATE INDEX idx_qr_codes_event ON qr_codes(event_id);
CREATE INDEX idx_qr_codes_hash ON qr_codes(code_hash);

CREATE TABLE check_ins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guest_id UUID REFERENCES guest_management.guests(id),
    event_id UUID REFERENCES event_management.events(id),
    device_id VARCHAR(255),
    gate VARCHAR(10),
    check_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced BOOLEAN DEFAULT true,
    CONSTRAINT unique_guest_event UNIQUE (guest_id, event_id)
);

CREATE INDEX idx_check_ins_event ON check_ins(event_id);
CREATE INDEX idx_check_ins_guest ON check_ins(guest_id);

CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id VARCHAR(255) UNIQUE NOT NULL,
    event_id UUID REFERENCES event_management.events(id),
    last_sync TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);