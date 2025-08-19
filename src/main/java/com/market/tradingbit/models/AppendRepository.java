package com.market.tradingbit.models;

import com.market.tradingbit.dtos.SwapDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AppendRepository {
    SwapDto swap;
    Long userId;
    String toName;
    BigDecimal quantityPriceTo;
    BigDecimal exactSwapAmount;
}
