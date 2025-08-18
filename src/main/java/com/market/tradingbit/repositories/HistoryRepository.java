package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface HistoryRepository extends JpaRepository<History, Long> {

    @Query("SELECT h.toQuantity FROM History h WHERE h.id = :id")
    BigDecimal getToQuantityById(Long id);
}
