-- V9__add_carried_over_days.sql (idempotent)
ALTER TABLE employees ADD COLUMN IF NOT EXISTS carried_over_days INTEGER DEFAULT 0;
