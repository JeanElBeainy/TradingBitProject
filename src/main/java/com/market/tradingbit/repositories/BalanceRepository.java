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
    @Query(value = "UPDATE balance b " +
            "JOIN portfolio p ON p.user_id = :userId AND p.symbol = :usdSymbol " +
            "SET b.usd_balance = p.quantity " +
            "WHERE b.id = :userId", nativeQuery = true)
    void syncUSDBalanceFromPortfolio(@Param("userId") Long userId, @Param("usdSymbol") String usdSymbol);

    @Modifying
    @Transactional
    @Query("UPDATE Balance b SET b.cryptoBalance = :amount WHERE b.id = :userId")
    void updateCryptoBalanceByAmountAndUserId(@Param("amount") BigDecimal amount, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Balance b SET b.totalVolume = (b.totalVolume + :volume) WHERE b.id = :userId")
    void updateTotalVolumeByAmountAndUserId(@Param("volume") BigDecimal volume, @Param("userId") Long userId);
}
