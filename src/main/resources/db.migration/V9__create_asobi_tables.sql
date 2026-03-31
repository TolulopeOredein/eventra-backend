-- ============================================
-- ASO-EBI SCHEMA
-- ============================================

SET search_path TO asobi;

CREATE TABLE collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    fabric_type VARCHAR(100),
    color VARCHAR(50),
    color_code VARCHAR(7),
    price DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'NGN',
    sample_images TEXT[] NOT NULL,
    sample_video_url TEXT,
    fabric_closeup_url TEXT,
    swatch_image_url TEXT,
    sizes JSONB NOT NULL,
    size_guide_url TEXT,
    delivery_methods TEXT[],
    pickup_locations JSONB,
    delivery_fee DECIMAL(12,2),
    international_shipping_available BOOLEAN DEFAULT false,
    international_shipping_fee JSONB,
    order_deadline TIMESTAMP NOT NULL,
    expected_delivery_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID REFERENCES collections(id),
    guest_id UUID REFERENCES guest_management.guests(id),
    event_id UUID REFERENCES event_management.events(id),
    size VARCHAR(10) NOT NULL,
    quantity INT DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    delivery_method VARCHAR(20),
    pickup_location_id UUID,
    delivery_address JSONB,
    shipping_fee DECIMAL(12,2),
    payment_status VARCHAR(20) DEFAULT 'pending',
    payment_reference VARCHAR(255),
    paid_at TIMESTAMP,
    delivery_status VARCHAR(20) DEFAULT 'pending',
    tracking_number VARCHAR(255),
    courier VARCHAR(100),
    shipped_at TIMESTAMP,
    actual_delivery_date TIMESTAMP,
    delivered_confirmed_by_guest BOOLEAN DEFAULT false,
    delivered_confirmed_at TIMESTAMP,
    delivered_photo_url TEXT,
    delivery_rating INT,
    delivery_feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE delivery_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id),
    event_type VARCHAR(50) NOT NULL,
    event_description TEXT,
    event_location TEXT,
    event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recorded_by UUID REFERENCES user_management.users(id),
    photo_url TEXT
);