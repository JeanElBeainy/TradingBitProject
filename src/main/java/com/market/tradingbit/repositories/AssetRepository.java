package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, String> {
}
