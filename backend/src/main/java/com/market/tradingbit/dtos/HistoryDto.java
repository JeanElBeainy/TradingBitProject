package com.market.tradingbit.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class HistoryDto {
    private final String fromSymbol;
    private final String fromName;
    private BigDecimal fromQuantity;
    private final double fromPrice;

    private String toSymbol;
    private String toName;
    private double toPrice;
}
