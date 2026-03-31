-- ============================================
-- FOOD MANAGEMENT SCHEMA
-- ============================================

SET search_path TO food_management;

CREATE TABLE menus (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    price DECIMAL(10,2),
    dietary_type VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meal_selections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guest_id UUID REFERENCES guest_management.guests(id),
    event_id UUID REFERENCES event_management.events(id),
    menu_id UUID REFERENCES menus(id),
    selected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    served BOOLEAN DEFAULT false,
    served_at TIMESTAMP,
    rating INT,
    feedback TEXT
);

CREATE TABLE food_stations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    station_type VARCHAR(50),
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE station_inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID REFERENCES food_stations(id),
    menu_id UUID REFERENCES menus(id),
    quantity INT NOT NULL,
    initial_quantity INT NOT NULL,
    reorder_point INT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE consumption_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    station_id UUID REFERENCES food_stations(id),
    menu_id UUID REFERENCES menus(id),
    quantity_served INT NOT NULL,
    served_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    served_by UUID REFERENCES user_management.users(id)
);

CREATE TABLE food_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    predicted_attendance INT,
    predicted_meal_distribution JSONB,
    recommended_quantities JSONB,
    confidence_score DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);