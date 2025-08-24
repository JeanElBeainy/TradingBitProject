package com.market.tradingbit.services;

import com.market.tradingbit.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public CryptoNamePrice getCryptoNameBySymbol(String symbol) {
        List<CryptoInfo> allCryptos = getLatestListings();
        return allCryptos.stream()
                .filter(c -> c.getSymbol().equalsIgnoreCase(symbol))
                .map(c -> new CryptoNamePrice(
                        c.getName(),
                        c.getQuote().get("USD").getPrice()
                ))
                .findFirst()
                .orElse(null);
    }

    public List<CryptoNamePrice> getPricesBySymbols(String symbol1, String symbol2) {
        List<CryptoInfo> allCryptos = getLatestListings();

        Optional<CryptoNamePrice> crypto1 = allCryptos.stream()
                .filter(c -> c.getSymbol().equalsIgnoreCase(symbol1))
                .map(c -> new CryptoNamePrice(
                        c.getName(),
                        c.getQuote().get("USD").getPrice()
                ))
                .findFirst();

        Optional<CryptoNamePrice> crypto2 = allCryptos.stream()
                .filter(c -> c.getSymbol().equalsIgnoreCase(symbol2))
                .map(c -> new CryptoNamePrice(
                        c.getName(),
                        c.getQuote().get("USD").getPrice()
                ))
                .findFirst();

        List<CryptoNamePrice> result = new ArrayList<>();
        crypto1.ifPresent(result::add);
        crypto2.ifPresent(result::add);
        return result;
    }

    public BigDecimal calculatePortfolioValue(List<CryptoHolding> cryptoHoldings) {
        if (cryptoHoldings == null || cryptoHoldings.isEmpty())
            return BigDecimal.ZERO;

        List<CryptoInfo> allCryptos = getLatestListings();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (CryptoHolding holding : cryptoHoldings) {
            String symbol = holding.getSymbol();
            BigDecimal quantity = holding.getQuantity();

            Optional<CryptoInfo> cryptoInfo = allCryptos.stream()
                    .filter(c -> c.getSymbol().equalsIgnoreCase(symbol))
                    .findFirst();

            if (cryptoInfo.isPresent()) {
                BigDecimal price = BigDecimal.valueOf(cryptoInfo.get().getQuote().get("USD").getPrice());
                BigDecimal value = price.multiply(quantity);
                totalValue = totalValue.add(value);
            } else //if crypto falls off the top 100 range, since the API gives only the top 100
                System.out.println("No such symbol: " + symbol);
        }
        return totalValue;
    }

    public List<CryptoSymbolPrice> getAllCryptoSymbolPrices() {
        List<CryptoInfo> allCryptos = getLatestListings();

        if (allCryptos == null || allCryptos.isEmpty()) {
            return Collections.emptyList();
        }

        return allCryptos.stream()
                .map(crypto -> {
                    CryptoSymbolPrice cryptoSymbolPrice = new CryptoSymbolPrice();
                    cryptoSymbolPrice.setName(crypto.getName());
                    cryptoSymbolPrice.setSymbol(crypto.getSymbol());
                    cryptoSymbolPrice.setPrice(crypto.getQuote().get("USD").getPrice());
                    return cryptoSymbolPrice;
                })
                .toList();
    }
}
