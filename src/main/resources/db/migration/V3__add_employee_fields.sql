-- Add new columns to employees table (idempotent)
ALTER TABLE employees ADD COLUMN IF NOT EXISTS position VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS tour_number VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS standort VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS profile_photo_url VARCHAR(255);
