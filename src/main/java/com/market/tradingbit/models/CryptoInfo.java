package com.market.tradingbit.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class CryptoInfo {
    private int id;
    private String name;
    private String symbol;
    private final String USD = "USD";

    @JsonProperty("quote")
    private Map<String, UsdQuote> quote;

    @JsonProperty("circulating_supply")
    private double circulatingSupply;

    @JsonProperty("max_supply")
    private Double maxSupply;

    public double getPrice() {
        return quote.get(USD).getPrice();
    }

    public double getMarketCap() {
        return quote.get(USD).getMarketCap();
    }

    public double getVolume24h() {
        return quote.get(USD).getVolume24h();
    }

    public double getPercentChange24h() {
        return quote.get(USD).getPercentChange24h();
    }
}

