package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.Portfolio;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    //@EntityGraph(attributePaths = {"user_id"})
    //@Query("SELECT p FROM Portfolio p WHERE p.userId = :user_id")
    List<Portfolio> getPortfolioByUserId(Long userId);

    @Query("SELECT p.symbol FROM Portfolio p WHERE p.userId = :userId")
    List<String> getPortfolioSymbolsByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Portfolio p SET p.quantity = (p.quantity + :quantity) WHERE p.symbol = :symbol AND p.userId = :userId")
    void updatePortfolioQuantity(@Param("quantity") float quantity, @Param("symbol") String symbol, @Param("userId") Long userId);
}
