-- Add indexes to optimize the most frequent query patterns.

-- portfolio(user_id, symbol): used by getItemBySymbolAndUserId, getQuantityBySymbolAndUserId,
-- updatePortfolioQuantity, deletePortfolioByQuantityIsLessThanEqualAndUserIdAndSymbol
CREATE INDEX idx_portfolio_user_symbol ON portfolio (user_id, symbol);

-- portfolio(user_id): used by getCryptoPortfolioByUserId, getCryptoSymbolAndQuantityByUserId
CREATE INDEX idx_portfolio_user_id ON portfolio (user_id);

-- history(user_id, id DESC): used by findTop3ByUserIdOrderByIdDesc, findAllByIdDesc
CREATE INDEX idx_history_user_id ON history (user_id, id DESC);
