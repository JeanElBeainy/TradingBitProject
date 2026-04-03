package com.market.tradingbit.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CryptoSymbolPrice {
    private String name;
    private String symbol;
    private Double price;
}
