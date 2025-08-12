package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
