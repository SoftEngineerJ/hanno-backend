-- Add deletedBy and deleteReason fields to employees table
ALTER TABLE employees 
ADD COLUMN deleted_by VARCHAR(50),
ADD COLUMN delete_reason VARCHAR(100);
