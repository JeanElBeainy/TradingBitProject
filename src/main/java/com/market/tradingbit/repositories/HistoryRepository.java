package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    @Query("SELECT h.toQuantity FROM History h WHERE h.id = :id")
    BigDecimal getToQuantityById(Long id);

    List<History> findTop3ByUserIdOrderByIdDesc(Long userId);

    @Query("SELECT h FROM History h WHERE h.userId = :userId ORDER BY h.id DESC")
    List<History> findAllByIdDesc(Long userId);
}
