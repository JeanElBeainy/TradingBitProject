package com.market.tradingbit.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CryptoNamePrice {
    private String name;
    private Double price;
}
