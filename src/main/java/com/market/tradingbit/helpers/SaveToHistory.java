package com.market.tradingbit.helpers;

import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.models.AppendRepository;
import com.market.tradingbit.models.CurrencyConstant;
import com.market.tradingbit.models.SaveHistory;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@AllArgsConstructor
public class SaveToHistory {
    private static final int PRECISION = 8;
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.001");
    private final PortfolioRepository portfolioRepository;
    private final HistoryRepository historyRepository;
    private final BalanceRepository balanceRepository;

    private BigDecimal getBigDecimalQuantity(History history, BigDecimal exactSwapAmount) {
        return history.getFromPrice()
                .multiply(exactSwapAmount)
                .divide(history.getToPrice(), PRECISION, RoundingMode.HALF_EVEN);
    }

    private void appendToRepository(AppendRepository appendRepository) { //4 queries
        Portfolio portfolio = portfolioRepository.getItemBySymbolAndUserId(appendRepository.getSwap().getTo(), appendRepository.getUserId());
        if(portfolio == null) {
            if (appendRepository.getSwap().getTo().equals(CurrencyConstant.USDName))
                portfolioRepository.save(Portfolio.builder()
                        .symbol(appendRepository.getSwap().getTo())
                        .purchaseType(Type.USD)
                        .userId(appendRepository.getUserId())
                        .name(appendRepository.getToName())
                        .quantity(appendRepository.getQuantityPriceTo())
                        .build());
            else
                portfolioRepository.save(Portfolio.builder()
                        .symbol(appendRepository.getSwap().getTo())
                        .purchaseType(Type.CRYPTO)
                        .userId(appendRepository.getUserId())
                        .name(appendRepository.getToName())
                        .quantity(appendRepository.getQuantityPriceTo())
                        .build());
        }
        else
            portfolioRepository.updatePortfolioQuantity(appendRepository.getQuantityPriceTo(),
                    appendRepository.getSwap().getTo(),
                    appendRepository.getUserId());

        portfolioRepository.updatePortfolioQuantity(appendRepository.getExactSwapAmount().negate(),
                appendRepository.getSwap().getFrom(),
                appendRepository.getUserId());

        if(appendRepository.getSwap().getFrom().equals(CurrencyConstant.USDName)
                || appendRepository.getSwap().getTo().equals(CurrencyConstant.USDName))
            balanceRepository.updateUSDBalanceByAmountAndUserId(
                    portfolioRepository.getQuantityBySymbolAndUserId(CurrencyConstant.USDName, appendRepository.getUserId()),
                    appendRepository.getUserId());

        portfolioRepository.deletePortfolioByQuantityIsLessThanEqualAndUserIdAndSymbol(BigDecimal.ZERO,
                appendRepository.getUserId(),
                appendRepository.getSwap().getFrom());
    }

    public void saveHistory(SaveHistory saveHistory) { //1 query
        BigDecimal toQuantity = getBigDecimalQuantity(saveHistory.getHistory(), saveHistory.getExactSwapAmount());
        BigDecimal fee = toQuantity
                .multiply(FEE_PERCENTAGE)
                .setScale(PRECISION, RoundingMode.HALF_EVEN);

        BigDecimal finalToQuantity = toQuantity.subtract(fee);
        saveHistory.getHistory().setToQuantity(finalToQuantity);

        appendToRepository(new AppendRepository(saveHistory.getSwap(),
                saveHistory.getUserId(),
                saveHistory.getHistory().getToName(),
                finalToQuantity,
                saveHistory.getExactSwapAmount()));

        saveHistory.getHistory().setFee(fee);
        saveHistory.getHistory().setUserId(saveHistory.getUserId());
        saveHistory.getHistory().setVolume(saveHistory.getVolume());
        saveHistory.getHistory().setType(Type.CRYPTO);
        saveHistory.getHistory().setTime(saveHistory.getSwap().getDate());
        historyRepository.save(saveHistory.getHistory());
    }
}
