-- Make total_balance a GENERATED column computed from the three balance components.
-- This eliminates the possibility of the value ever being out of sync with its parts,
-- and removes the need to maintain it in application queries.
ALTER TABLE balance
    MODIFY COLUMN total_balance DECIMAL(30, 8)
        GENERATED ALWAYS AS (usd_balance + crypto_balance + stock_balance) STORED NOT NULL;
