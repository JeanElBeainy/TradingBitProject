package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.Portfolio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    //@EntityGraph(attributePaths = {"user_id"})
    //@Query("SELECT p FROM Portfolio p WHERE p.userId = :user_id")
    List<Portfolio> getPortfolioByUserId(Long userId);

}
