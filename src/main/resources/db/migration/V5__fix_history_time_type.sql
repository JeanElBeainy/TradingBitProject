-- Convert history.time from VARCHAR(30) to DATETIME.
-- Existing string values cannot be automatically converted from the custom
-- display format, so they are cleared and the column is made NOT NULL going forward.
ALTER TABLE history
    DROP COLUMN time;

ALTER TABLE history
    ADD COLUMN time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
