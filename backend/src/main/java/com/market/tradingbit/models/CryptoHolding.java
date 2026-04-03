package com.market.tradingbit.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CryptoHolding {
    private String symbol;
    private BigDecimal quantity;
}
