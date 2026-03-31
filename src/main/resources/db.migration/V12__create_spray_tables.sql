-- ============================================
-- VIRTUAL SPRAYING SCHEMA
-- ============================================

SET search_path TO virtual_spray;

CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES user_management.users(id) UNIQUE,
    default_denomination DECIMAL(12,2) DEFAULT 1000,
    default_notes_per_tap INT DEFAULT 1,
    default_animation VARCHAR(50) DEFAULT 'naira_rain',
    quick_options JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tap_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES user_management.users(id),
    event_id UUID REFERENCES event_management.events(id),
    denomination_per_tap DECIMAL(12,2) NOT NULL,
    notes_per_tap INT NOT NULL,
    total_reserved DECIMAL(15,2) NOT NULL,
    remaining_balance DECIMAL(15,2) NOT NULL,
    total_sprayed DECIMAL(15,2) DEFAULT 0,
    total_taps INT DEFAULT 0,
    animation_type VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tap_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID REFERENCES tap_sessions(id),
    user_id UUID REFERENCES user_management.users(id),
    event_id UUID REFERENCES event_management.events(id),
    denomination_used DECIMAL(12,2) NOT NULL,
    notes_used INT NOT NULL,
    tap_amount DECIMAL(12,2) NOT NULL,
    tap_number INT NOT NULL,
    running_total DECIMAL(15,2) NOT NULL,
    tapped_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE spray_leaderboard (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    user_id UUID REFERENCES user_management.users(id),
    total_amount DECIMAL(15,2) DEFAULT 0,
    total_taps INT DEFAULT 0,
    rank_position INT,
    last_spray_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_id, user_id)
);

CREATE TABLE animations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    key VARCHAR(50) UNIQUE,
    description TEXT,
    preview_url TEXT,
    is_premium BOOLEAN DEFAULT false,
    premium_price DECIMAL(10,2),
    platform_fee_percentage DECIMAL(5,2) DEFAULT 7.0,
    is_active BOOLEAN DEFAULT true
);

INSERT INTO animations (name, key, description, is_premium, platform_fee_percentage) VALUES
('Naira Rain', 'naira_rain', 'Traditional Naira notes raining down', false, 5.0),
('Dollar Rain', 'dollar_rain', 'Green dollar bills shower', true, 7.0),
('Golden Dollar', 'gold_dollar', 'Luxurious golden dollar effect', true, 10.0),
('Confetti', 'confetti', 'Colorful confetti celebration', false, 6.0);