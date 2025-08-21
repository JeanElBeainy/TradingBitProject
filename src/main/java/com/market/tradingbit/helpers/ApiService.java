package com.market.tradingbit.helpers;

import com.market.tradingbit.models.CryptoNamePrice;
import com.market.tradingbit.models.CryptoNameSymbol;
import com.market.tradingbit.services.CoinMarketCapService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ApiService {

    private final CoinMarketCapService service;

    public CryptoNamePrice getCryptoPrice(String symbol) {
        return service.getCryptoNameBySymbol(symbol);
    }

    public List<CryptoNamePrice> getCryptoPair(String symbol1, String symbol2) {
        return service.getPricesBySymbols(symbol1, symbol2);
    }
}
