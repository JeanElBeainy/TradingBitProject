package com.market.tradingbit.dtos;

import lombok.Data;

@Data
public class SwapDto {
    private String from;
    private String to;
    private double quantity;
}
