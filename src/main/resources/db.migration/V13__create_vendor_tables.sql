-- ============================================
-- VENDOR MANAGEMENT SCHEMA
-- ============================================

SET search_path TO vendor_management;

CREATE TABLE vendors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES user_management.users(id),
    business_name VARCHAR(255) NOT NULL,
    business_registration_number VARCHAR(100),
    vendor_type VARCHAR(50) NOT NULL,
    category VARCHAR(50),
    contact_person VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    services_offered TEXT[],
    service_area TEXT[],
    price_range_min DECIMAL(10,2),
    price_range_max DECIMAL(10,2),
    verified BOOLEAN DEFAULT false,
    verification_documents JSONB,
    rating DECIMAL(3,2),
    review_count INT DEFAULT 0,
    portfolio_urls TEXT[],
    social_media_links JSONB,
    availability_calendar JSONB,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vendor_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id UUID REFERENCES vendors(id),
    event_id UUID REFERENCES event_management.events(id),
    reviewer_id UUID REFERENCES user_management.users(id),
    rating INT CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    categories JSONB,
    verified_hire BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_vendors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    vendor_id UUID REFERENCES vendors(id),
    role VARCHAR(100) NOT NULL,
    contract_amount DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'NGN',
    payment_status VARCHAR(20) DEFAULT 'pending',
    payment_terms TEXT,
    arrival_time TIMESTAMP,
    departure_time TIMESTAMP,
    setup_time_needed INT,
    staff_count INT DEFAULT 1,
    equipment_needed JSONB,
    special_requirements TEXT,
    checked_in BOOLEAN DEFAULT false,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    performance_rating INT,
    payment_released BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);