-- V7__update_admin_password_to_bcrypt.sql
-- Update default admin password to BCrypt hash

-- BCrypt hash for '54321'
UPDATE admins 
SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE username = 'admin';
