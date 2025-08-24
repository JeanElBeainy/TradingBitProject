package com.market.tradingbit.models;

import lombok.Data;

@Data
public class CryptoSymbolPrice {
    private String name;
    private String symbol;
    private Double price;
}
