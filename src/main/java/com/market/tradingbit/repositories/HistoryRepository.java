package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {

}
