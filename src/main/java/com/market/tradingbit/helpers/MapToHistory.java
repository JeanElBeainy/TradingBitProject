package com.market.tradingbit.helpers;

import com.market.tradingbit.dtos.HistoryDto;
import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.History;
import com.market.tradingbit.mappers.HistoryMapper;
import com.market.tradingbit.models.CryptoNamePrice;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
@AllArgsConstructor
public class MapToHistory {

    private final HistoryMapper historyMapper;

    public History fromUSDtoHistory(SwapDto swap, CryptoNamePrice price, BigDecimal exactQuantity) {
        HistoryDto historyDto = new HistoryDto("US Dollar",
                "US Dollar Balance",
                exactQuantity,
                BigDecimal.ONE,
                swap.getTo(),
                price.getName(),
                BigDecimal.valueOf(price.getPrice()));
        return historyMapper.toHistory(historyDto);
    }

    public History toUSDtoHistory(SwapDto swap, CryptoNamePrice price, BigDecimal exactQuantity) {
        HistoryDto historyDto = new HistoryDto(swap.getFrom(),
                price.getName(),
                exactQuantity,
                BigDecimal.valueOf(price.getPrice()),
                "US Dollar",
                "US Dollar Balance",
                BigDecimal.ONE);
        return historyMapper.toHistory(historyDto);
    }

    public History toHistory(SwapDto swap, List<CryptoNamePrice> prices, BigDecimal exactQuantity) {
        HistoryDto historyDto = new HistoryDto(swap.getFrom(),
                prices.get(0).getName(),
                exactQuantity,
                BigDecimal.valueOf(prices.get(0).getPrice()),
                swap.getTo(),
                prices.get(prices.size() - 1).getName(),
                BigDecimal.valueOf(prices.get(prices.size() - 1).getPrice()));
        return historyMapper.toHistory(historyDto);
    }
}
