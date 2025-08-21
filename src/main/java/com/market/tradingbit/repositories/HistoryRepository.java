package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    @Query("SELECT h.toQuantity FROM History h WHERE h.id = :id")
    BigDecimal getToQuantityById(Long id);

    @Query("SELECT h FROM History h WHERE h.id = :id AND (h.type = :type OR h.type = 'USD')")
    List<History> findTop3ByUserIdAndTypeOrderByIdDesc(Long userId, Type type);

    List<History> findTop3ByUserIdOrderByIdDesc(Long userId);

    @Query("SELECT h FROM History h WHERE h.userId = :userId ORDER BY h.id DESC")
    List<History> findAllByIdDesc(Long userId);
}
