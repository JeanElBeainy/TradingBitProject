package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.Portfolio;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> getPortfolioByUserId(Long userId);

    @Query("SELECT p.symbol FROM Portfolio p WHERE p.userId = :userId")
    List<String> getPortfolioSymbolsByUserId(Long userId);

    @Query("SELECT p FROM Portfolio p WHERE p.userId = :userId AND p.purchaseType = 'CRYPTO'")
    List<Portfolio> getCryptoPortfolioByUserId(Long userId);

    @Query("SELECT p FROM Portfolio p WHERE p.userId = :userId AND p.purchaseType = 'STOCK'")
    List<Portfolio> getStockPortfolioByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Portfolio p SET p.quantity = (p.quantity + :quantity) WHERE p.symbol = :symbol AND p.userId = :userId")
    void updatePortfolioQuantity(@Param("quantity") float quantity, @Param("symbol") String symbol, @Param("userId") Long userId);
}
