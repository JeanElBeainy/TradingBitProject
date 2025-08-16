package com.market.tradingbit.services;

import com.market.tradingbit.models.CmcResponse;
import com.market.tradingbit.models.CryptoInfo;
import com.market.tradingbit.models.CryptoNamePrice;
import com.market.tradingbit.models.CryptoNameSymbol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CoinMarketCapService {
    private final WebClient webClient;

    public CoinMarketCapService(@Value("${coinmarketcap.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://pro-api.coinmarketcap.com/v1")
                .defaultHeader("X-CMC_PRO_API_KEY", apiKey)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public List<CryptoInfo> getLatestListings() {
        CmcResponse response = webClient.get()
                .uri("/cryptocurrency/listings/latest")
                .retrieve()
                .bodyToMono(CmcResponse.class)
                .block();

        if(response == null)
            return Collections.emptyList();

        return response.getData();
    }

    public List<CryptoNameSymbol> getLatestNameAndSymbol() {
        List<CryptoNameSymbol> list = getLatestListings().stream()
                .map(c -> new CryptoNameSymbol(c.getName(), c.getSymbol()))
                .toList();

        List<CryptoNameSymbol> result = new ArrayList<>(list);
        result.add(new CryptoNameSymbol("US Dollar Balance", "US Dollar"));
        return result;
    }

    public List<CryptoNamePrice> getPricesBySymbols(String symbol1, String symbol2) {
        List<CryptoInfo> allCryptos = getLatestListings();

        return allCryptos.stream()
                .filter(c -> c.getSymbol().equalsIgnoreCase(symbol1) || c.getSymbol().equalsIgnoreCase(symbol2))
                .map(c -> new CryptoNamePrice(
                        c.getName(),
                        c.getQuote().get("USD").getPrice()
                ))
                .toList();
    }

}
