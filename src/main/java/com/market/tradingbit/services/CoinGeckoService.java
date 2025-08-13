package com.market.tradingbit.services;

import com.market.tradingbit.dtos.ChartData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CoinGeckoService {

    private final WebClient webClient;

    public CoinGeckoService(@Value("${coingecko.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.coingecko.com/api/v3")
                .defaultHeader("x-cg-demo-api-key", apiKey)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "TradingBit")
                .build();
    }

    public ChartData getCryptoChartData(String coinId, int days) {
        ChartData response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/coins/{id}/market_chart")
                        .queryParam("vs_currency", "usd")
                        .queryParam("days", days)
                        .build(coinId))
                .retrieve()
                .bodyToMono(ChartData.class)
                .block();
        if(response == null)
            return new ChartData();
        return response;
    }
}
