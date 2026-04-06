CREATE TABLE vacation_accounts (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL,
    year INTEGER NOT NULL,
    vacation_entitlement INTEGER DEFAULT 30,
    carried_over INTEGER DEFAULT 0,
    carried_over_expiry DATE,
    initial_used_days INTEGER DEFAULT 0,
    special_leave_initial INTEGER DEFAULT 0,
    compensation_initial INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(employee_id, year)
);
