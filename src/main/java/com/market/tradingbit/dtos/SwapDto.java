package com.market.tradingbit.dtos;

import lombok.Data;

@Data
public class SwapDto {
    private String from;
    private double promisedFromPrice;
    private String to;
    private double promisedToPrice;
    private String quantity;
}
