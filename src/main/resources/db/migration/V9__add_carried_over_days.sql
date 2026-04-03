-- V9__add_carried_over_days.sql
ALTER TABLE employees ADD COLUMN carried_over_days INTEGER DEFAULT 0;
