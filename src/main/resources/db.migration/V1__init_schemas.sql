-- Create all schemas for Evantra
CREATE SCHEMA IF NOT EXISTS user_management;
CREATE SCHEMA IF NOT EXISTS event_management;
CREATE SCHEMA IF NOT EXISTS guest_management;
CREATE SCHEMA IF NOT EXISTS entry_management;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS food_management;
CREATE SCHEMA IF NOT EXISTS photo_hub;
CREATE SCHEMA IF NOT EXISTS asobi;
CREATE SCHEMA IF NOT EXISTS seating;
CREATE SCHEMA IF NOT EXISTS streaming;
CREATE SCHEMA IF NOT EXISTS virtual_spray;
CREATE SCHEMA IF NOT EXISTS vendor_management;
CREATE SCHEMA IF NOT EXISTS crew_management;
CREATE SCHEMA IF NOT EXISTS emergency;
CREATE SCHEMA IF NOT EXISTS digital_registry;
CREATE SCHEMA IF NOT EXISTS guest_lifecycle;
CREATE SCHEMA IF NOT EXISTS guest_communication;
CREATE SCHEMA IF NOT EXISTS payment_escrow;
CREATE SCHEMA IF NOT EXISTS marketplace;
CREATE SCHEMA IF NOT EXISTS white_label;

-- Set search path for each schema
SET search_path TO user_management;