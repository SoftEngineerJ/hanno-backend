CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50),
    fcm_token VARCHAR(500),
    profile_photo_url VARCHAR(500)
);

-- Test-Benutzer (Passwort: 54321 - BCrypt hash)
INSERT INTO employees (email, username, password, first_name, last_name, role)
VALUES ('admin@hanno.de', 'admin', '$2a$10$ZAG.d0v/7Zxp1u5CmLwfWeJSYJHQMwDExRUXDdpmDDmTNzjHcg.ke', 'Admin', 'User', 'admin');