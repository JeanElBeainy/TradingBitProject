-- The code assumes at most one portfolio row per (user_id, symbol) pair.
-- Upgrade the existing index to a unique constraint to enforce this at the database level.
DROP INDEX idx_portfolio_user_symbol ON portfolio;

ALTER TABLE portfolio
    ADD CONSTRAINT uq_portfolio_user_symbol UNIQUE (user_id, symbol);
