package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    @Query("SELECT p FROM Portfolio p WHERE p.id = :id")
    List<Portfolio> getPortfolioById(@Param("id") Long id);
}
