package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.models.CryptoHolding;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    @Query("SELECT p FROM Portfolio p JOIN FETCH p.asset WHERE p.userId = :userId AND (p.purchaseType = 'CRYPTO' OR p.purchaseType = 'USD')")
    List<Portfolio> getCryptoPortfolioByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Portfolio p WHERE p.symbol = :symbol AND p.userId = :userId")
    Portfolio getItemBySymbolAndUserId(@Param("symbol") String symbol, @Param("userId") Long userId);

    @Query("SELECT p.quantity FROM Portfolio p WHERE p.symbol = :symbol AND p.userId = :userId")
    BigDecimal getQuantityBySymbolAndUserId(@Param("symbol") String symbol, @Param("userId") Long userId);

    @Query("SELECT p.symbol as symbol, p.quantity as quantity FROM Portfolio p WHERE p.userId = :userId AND p.purchaseType = 'CRYPTO'")
    List<CryptoHolding> getCryptoSymbolAndQuantityByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Portfolio p SET p.quantity = (p.quantity + :quantity) WHERE p.symbol = :symbol AND p.userId = :userId")
    void updatePortfolioQuantity(@Param("quantity") BigDecimal quantity, @Param("symbol") String symbol,
            @Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Transactional
    void deletePortfolioByQuantityIsLessThanEqualAndUserIdAndSymbol(BigDecimal quantity, Long userId, String symbol);
}
