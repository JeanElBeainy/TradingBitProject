package com.market.tradingbit.models;

import com.market.tradingbit.entities.History;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class HistoryVolume {
    private History history;
    private BigDecimal volume;
}
