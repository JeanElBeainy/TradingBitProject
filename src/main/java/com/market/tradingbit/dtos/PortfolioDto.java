package com.market.tradingbit.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class PortfolioDto {
    private String name;
    private String symbol;
    private String quantity;
    private String price;
}
