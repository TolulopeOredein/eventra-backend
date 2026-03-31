-- ============================================
-- PHOTO HUB SCHEMA
-- ============================================

SET search_path TO photo_hub;

CREATE TABLE event_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    uploader_id UUID REFERENCES user_management.users(id),
    photo_url TEXT NOT NULL,
    thumbnail_url TEXT,
    is_professional BOOLEAN DEFAULT false,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    quality_score DECIMAL(3,2),
    width INT,
    height INT
);

CREATE INDEX idx_photos_event ON event_photos(event_id);
CREATE INDEX idx_photos_uploader ON event_photos(uploader_id);

CREATE TABLE photo_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    photo_id UUID REFERENCES event_photos(id),
    guest_id UUID REFERENCES guest_management.guests(id),
    tag_type VARCHAR(20),
    confidence DECIMAL(3,2),
    bounding_box JSONB,
    tagged_by VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tags_photo ON photo_tags(photo_id);
CREATE INDEX idx_tags_guest ON photo_tags(guest_id);

CREATE TABLE photo_moments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    moment_type VARCHAR(50),
    photo_id UUID REFERENCES event_photos(id),
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confidence DECIMAL(3,2)
);

CREATE TABLE albums (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event_management.events(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cover_photo_id UUID REFERENCES event_photos(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE album_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    album_id UUID REFERENCES albums(id),
    photo_id UUID REFERENCES event_photos(id),
    display_order INT DEFAULT 0,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);