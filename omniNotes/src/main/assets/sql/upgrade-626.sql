ALTER TABLE notes ADD COLUMN trigger_type INTEGER DEFAULT 0;
ALTER TABLE notes ADD COLUMN trigger_location_radius REAL;
ALTER TABLE notes ADD COLUMN trigger_time_start TEXT;
ALTER TABLE notes ADD COLUMN trigger_time_end TEXT;