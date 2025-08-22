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
                1,
                swap.getTo(),
                price.getName(),
                price.getPrice()
        );
        return historyMapper.toHistory(historyDto);
    }

    public History toUSDtoHistory(SwapDto swap, CryptoNamePrice price, BigDecimal exactQuantity) {
        HistoryDto historyDto = new HistoryDto(swap.getFrom(),
                price.getName(),
                exactQuantity,
                price.getPrice(),
                "US Dollar",
                "US Dollar Balance",
                1
        );
        return historyMapper.toHistory(historyDto);
    }

    public History toHistory(SwapDto swap, List<CryptoNamePrice> prices, BigDecimal exactQuantity) {
        HistoryDto historyDto = new HistoryDto(swap.getFrom(),
                prices.getFirst().getName(),
                exactQuantity,
                prices.getFirst().getPrice(),
                swap.getTo(),
                prices.getLast().getName(),
                prices.getLast().getPrice()
        );
        return historyMapper.toHistory(historyDto);
    }
}
