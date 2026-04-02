-- V8__reset_admins_table.sql
-- Reset admins table with correct superadmin

DROP TABLE IF EXISTS admins;

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

-- Insert superadmin with password 54321
INSERT INTO admins (email, username, password, first_name, last_name, role)
VALUES ('admin@hannomed.de', 'admin', '54321', 'Admin', 'User', 'superadmin');
