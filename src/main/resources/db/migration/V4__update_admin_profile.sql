-- Update existing admin user with profile data
UPDATE employees SET position = 'Administrator', tour_number = '', standort = 'Hannover' WHERE username = 'admin';
