-- Time-Off Requests Tabelle
CREATE TABLE time_off_requests (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    requested_days INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'wartend',
    created_at DATE,
    updated_at DATE
);

-- Index für schnellere Abfragen
CREATE INDEX idx_time_off_employee_id ON time_off_requests(employee_id);
CREATE INDEX idx_time_off_start_date ON time_off_requests(start_date);
