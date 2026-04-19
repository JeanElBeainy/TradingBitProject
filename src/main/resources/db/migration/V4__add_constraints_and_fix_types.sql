-- Add unique constraint on users.email to enforce uniqueness at the database level
ALTER TABLE users
    ADD CONSTRAINT users_email_unique UNIQUE (email);

-- Add foreign key constraints that were missing from the initial schema
ALTER TABLE portfolio
    ADD CONSTRAINT portfolio_user_id_fk
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE history
    ADD CONSTRAINT history_user_id_fk
        FOREIGN KEY (user_id) REFERENCES users (id);

-- Fix history price column precision to match the rest of the schema (DECIMAL(30, 8))
ALTER TABLE history
    MODIFY from_price DECIMAL(30, 8) NOT NULL;

ALTER TABLE history
    MODIFY to_price DECIMAL(30, 8) NOT NULL;
