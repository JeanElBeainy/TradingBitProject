package com.market.tradingbit.models;

import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SaveHistory {
    History history;
    SwapDto swap;
    Long userId;
    BigDecimal volume;
    BigDecimal exactSwapAmount;
    Type type;
}
