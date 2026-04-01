-- V6__create_admins_table.sql
-- Admin table for HannoAdmin portal

CREATE TABLE admins (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) DEFAULT 'admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin user (password: admin123 - change in production!)
INSERT INTO admins (email, username, password, first_name, last_name, role)
VALUES ('admin@hannomed.de', 'admin', 'admin123', 'Admin', 'User', 'admin');
