package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.Balance;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BalanceRepository extends JpaRepository<Balance, Long> {
}
