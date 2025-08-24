package com.market.tradingbit.dtos;

import lombok.Data;

@Data
public class SwapDto {
    private String from;
    private String promisedFromPrice;
    private String to;
    private String promisedToPrice;
    private String quantity;
}
