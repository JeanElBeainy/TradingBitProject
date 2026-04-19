-- Normalize portfolio: extract asset names into a dedicated asset table.
-- portfolio.name violates 3NF because name is functionally dependent on symbol,
-- not on the primary key id.

CREATE TABLE asset
(
    symbol VARCHAR(10) NOT NULL,
    name   VARCHAR(50) NOT NULL,
    CONSTRAINT asset_symbol_pk PRIMARY KEY (symbol)
);

-- Populate asset table from existing portfolio data
INSERT IGNORE INTO asset (symbol, name)
SELECT DISTINCT symbol, name
FROM portfolio;

-- Add FK from portfolio.symbol to asset.symbol
ALTER TABLE portfolio
    ADD CONSTRAINT portfolio_symbol_fk
        FOREIGN KEY (symbol) REFERENCES asset (symbol);

-- Drop the now-redundant name column from portfolio
ALTER TABLE portfolio
    DROP COLUMN name;
