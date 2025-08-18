package com.market.tradingbit.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class FromUSD {
    private final String fromName = "US Dollar Balance";
    private final String fromSymbol = "USD";
    private BigDecimal fromQuantity;
    private final double fromPrice = 1;
    private String toName;
    private String toSymbol;
    private double toPrice;
}
