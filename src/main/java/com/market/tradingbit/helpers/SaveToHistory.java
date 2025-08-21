package com.market.tradingbit.helpers;

import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.models.AppendRepository;
import com.market.tradingbit.models.SaveHistory;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
public class SaveToHistory {
    private static final int CRYPTO_PRECISION = 8;
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.0025");
    private static final BigDecimal TOLERANCE = new BigDecimal("0.00000001");
    private final PortfolioRepository portfolioRepository;
    private final HistoryRepository historyRepository;

    private BigDecimal getBigDecimalQuantity(History history, BigDecimal exactSwapAmount) {
        BigDecimal fromPrice = new BigDecimal(String.valueOf(history.getFromPrice()))
                .setScale(CRYPTO_PRECISION, RoundingMode.HALF_EVEN);
        BigDecimal toPrice = new BigDecimal(String.valueOf(history.getToPrice()))
                .setScale(CRYPTO_PRECISION, RoundingMode.HALF_EVEN);

        return fromPrice
                .multiply(exactSwapAmount)
                .divide(toPrice, CRYPTO_PRECISION, RoundingMode.HALF_EVEN)
                .setScale(CRYPTO_PRECISION, RoundingMode.HALF_EVEN);
    }

    private void appendToRepository(AppendRepository appendRepository) {
        if(portfolioRepository.getItemBySymbolAndUserId(appendRepository.getSwap().getTo(), appendRepository.getUserId()) == null)
            portfolioRepository.save(Portfolio.builder()
                    .symbol(appendRepository.getSwap().getTo())
                    .purchaseType(Type.CRYPTO)
                    .userId(appendRepository.getUserId())
                    .name(appendRepository.getToName())
                    .quantity(appendRepository.getQuantityPriceTo())
                    .build());
        else
            portfolioRepository.updatePortfolioQuantity(appendRepository.getQuantityPriceTo(),
                    appendRepository.getSwap().getTo(),
                    appendRepository.getUserId());

        portfolioRepository.updatePortfolioQuantity(appendRepository.getExactSwapAmount().negate(),
                appendRepository.getSwap().getFrom(),
                appendRepository.getUserId());
        BigDecimal remainingQuantity = portfolioRepository.getItemBySymbolAndUserId(appendRepository.getSwap().getFrom(),
                appendRepository.getUserId()).getQuantity();

        if(remainingQuantity.abs().compareTo(TOLERANCE) <= 0)
            portfolioRepository.deleteById(portfolioRepository.getItemBySymbolAndUserId(appendRepository.getSwap().getFrom(), appendRepository.getUserId()).getId());
    }

    public void saveHistory(SaveHistory saveHistory) {
        BigDecimal toQuantity = getBigDecimalQuantity(saveHistory.getHistory(), saveHistory.getExactSwapAmount());
        BigDecimal fee = toQuantity
                .multiply(FEE_PERCENTAGE)
                .setScale(CRYPTO_PRECISION, RoundingMode.HALF_EVEN);

        BigDecimal finalToQuantity = toQuantity.subtract(fee);
        saveHistory.getHistory().setToQuantity(finalToQuantity);

        appendToRepository(new AppendRepository(saveHistory.getSwap(),
                saveHistory.getUserId(),
                saveHistory.getHistory().getToName(),
                finalToQuantity,
                saveHistory.getExactSwapAmount()));

        saveHistory.getHistory().setFee(fee);
        saveHistory.getHistory().setUserId(saveHistory.getUserId());
        saveHistory.getHistory().setVolume(saveHistory.getVolume().floatValue());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");
        saveHistory.getHistory().setTime(LocalDateTime.now().format(formatter));
        historyRepository.save(saveHistory.getHistory());
    }
}
