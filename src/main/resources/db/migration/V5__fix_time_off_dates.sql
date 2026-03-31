-- Fix column types for time_off_requests table
ALTER TABLE time_off_requests ALTER COLUMN created_at TYPE TIMESTAMP;
ALTER TABLE time_off_requests ALTER COLUMN updated_at TYPE TIMESTAMP;
