package com.market.tradingbit.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UsdQuote {
    private double price;
    @JsonProperty("market_cap")
    private double marketCap;
    @JsonProperty("volume_24h")
    private double volume24h;
    @JsonProperty("percent_change_24h")
    private double percentChange24h;
}