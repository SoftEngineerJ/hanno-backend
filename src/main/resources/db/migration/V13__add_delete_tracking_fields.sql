-- Add deletedBy and deleteReason fields to employees table (idempotent)
ALTER TABLE employees 
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(50),
ADD COLUMN IF NOT EXISTS delete_reason VARCHAR(100);
