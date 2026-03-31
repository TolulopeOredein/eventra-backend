-- ============================================
-- DIGITAL REGISTRY SCHEMA
-- ============================================

SET search_path TO digital_registry;

CREATE TABLE merchants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    api_type VARCHAR(50) NOT NULL,
    api_endpoint VARCHAR(255),
    commission_rate DECIMAL(5,2) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    category_whitelist TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID REFERENCES merchants(id),
    merchant_sku VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'NGN',
    image_url TEXT,
    category VARCHAR(100),
    brand VARCHAR(100),
    is_available BOOLEAN DEFAULT true,
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_registry_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    product_id UUID REFERENCES products(id),
    quantity_desired INT DEFAULT 1,
    quantity_purchased INT DEFAULT 0,
    is_priority BOOLEAN DEFAULT false,
    notes TEXT
);

CREATE TABLE gifts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    contributor_id UUID REFERENCES user_management.users(id),
    contributor_name VARCHAR(255) NOT NULL,
    contributor_phone VARCHAR(20),
    contributor_message TEXT,
    gift_type VARCHAR(20) NOT NULL,
    product_id UUID REFERENCES products(id),
    merchant_id UUID REFERENCES merchants(id),
    merchant_order_id VARCHAR(255),
    item_name VARCHAR(255),
    quantity INT DEFAULT 1,
    amount DECIMAL(12,2),
    currency VARCHAR(3),
    subtotal DECIMAL(12,2),
    platform_fee DECIMAL(12,2),
    merchant_commission DECIMAL(12,2),
    net_to_recipient DECIMAL(12,2),
    delivery_status VARCHAR(20) DEFAULT 'pending',
    tracking_number VARCHAR(255),
    show_on_gift_wall BOOLEAN DEFAULT true,
    show_contributor_name BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE gift_wall_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    gift_id UUID REFERENCES gifts(id),
    display_text TEXT,
    display_icon VARCHAR(50),
    displayed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);