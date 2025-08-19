package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.Balance;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;


public interface BalanceRepository extends JpaRepository<Balance, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Balance b SET b.usdBalance = :amount WHERE b.id = :userId")
    void updateUSDBalanceByAmountAndUserId(@Param("amount") BigDecimal amount, Long userId);
}
