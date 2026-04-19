package com.market.tradingbit.helpers;

import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.entities.Asset;
import com.market.tradingbit.models.AppendRepository;
import com.market.tradingbit.models.CurrencyConstant;
import com.market.tradingbit.models.SaveHistory;
import com.market.tradingbit.repositories.AssetRepository;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@AllArgsConstructor
public class SaveToHistory {
    private static final int PRECISION = 8;
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.001");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a", Locale.ENGLISH);
    private final PortfolioRepository portfolioRepository;
    private final HistoryRepository historyRepository;
    private final BalanceRepository balanceRepository;
    private final AssetRepository assetRepository;

    private BigDecimal getBigDecimalQuantity(History history, BigDecimal exactSwapAmount) {
        return history.getFromPrice()
                .multiply(exactSwapAmount)
                .divide(history.getToPrice(), PRECISION, RoundingMode.HALF_EVEN);
    }

    private void appendToRepository(AppendRepository appendRepository) { //4 queries
        Portfolio portfolio = portfolioRepository.getItemBySymbolAndUserId(appendRepository.getSwap().getTo(), appendRepository.getUserId());
        if(portfolio == null) {
            assetRepository.save(Asset.builder()
                    .symbol(appendRepository.getSwap().getTo())
                    .name(appendRepository.getToName())
                    .build());
            Type type = appendRepository.getSwap().getTo().equals(CurrencyConstant.USDName) ? Type.USD : Type.CRYPTO;
            portfolioRepository.save(Portfolio.builder()
                    .symbol(appendRepository.getSwap().getTo())
                    .purchaseType(type)
                    .userId(appendRepository.getUserId())
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
            balanceRepository.syncUSDBalanceFromPortfolio(appendRepository.getUserId(), CurrencyConstant.USDName);

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
        saveHistory.getHistory().setType(Type.CRYPTO);
        saveHistory.getHistory().setVolume(saveHistory.getVolume());
        LocalDateTime tradeTime;
        try {
            tradeTime = LocalDateTime.parse(saveHistory.getSwap().getDate(), DATE_FORMATTER);
        } catch (Exception e) {
            tradeTime = LocalDateTime.now();
        }
        saveHistory.getHistory().setTime(tradeTime);
        historyRepository.save(saveHistory.getHistory());
    }
}
