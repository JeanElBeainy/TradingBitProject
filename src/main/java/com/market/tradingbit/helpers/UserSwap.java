package com.market.tradingbit.helpers;

import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.SwapErrorType;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.models.*;
import com.market.tradingbit.models.Error;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.market.tradingbit.helpers.SwapValidation.parseQuantity;

@Component
@AllArgsConstructor
public class UserSwap {
    private final PortfolioRepository portfolioRepository;
    private final SwapValidation swapValidation;
    private static final int CRYPTO_PRECISION = 8;
    private final BalanceRepository balanceRepository;
    private final SaveToHistory saveToHistory;
    private final MapToHistory mapToHistory;
    private final ApiService apiService;
    private final BigDecimal TOLERANCE = new BigDecimal("0.00000001");
    private final BigDecimal MINIMUM_SWAP_USD = new BigDecimal("1.00");

    private BigDecimal getExactSwapAmount(BigDecimal availableBalance, BigDecimal requestedAmount) {
        BigDecimal difference = requestedAmount.subtract(availableBalance);
        if (difference.compareTo(BigDecimal.ZERO) > 0 && difference.compareTo(TOLERANCE) <= 0)
            return availableBalance;
        return requestedAmount;
    }

    private void validateQuantity(BigDecimal swapQuantity, Error error, Double price) {
        if((swapQuantity.multiply(BigDecimal.valueOf(price))).compareTo(MINIMUM_SWAP_USD) < 0)
            swapValidation.swapError(error, SwapErrorType.QUANTITY);
    }

    private HistoryVolume swapFrom(SwapDto swap, Error error, BigDecimal swapQuantity , BigDecimal exactSwapAmount) {
        CryptoNamePrice price = apiService.getCryptoPrice(swap.getTo());
        validateQuantity(swapQuantity, error, price.getPrice());
        History history = mapToHistory.fromUSDtoHistory(swap, price, exactSwapAmount);
        return new HistoryVolume(history, exactSwapAmount);
    }

    public String userSwap(Model model, SwapDto swap, Long userId, BindingResult bindingResult) {
        BigDecimal volume;
        History history;

        BigDecimal swapQuantity = parseQuantity(swap.getQuantity());
        BigDecimal availableBalance = portfolioRepository.getQuantityBySymbolAndUserId(swap.getFrom(), userId);
        BasicUserError basicUserError = new BasicUserError(model, swapQuantity, swap, userId, bindingResult);
        String basicErrors = swapValidation.checkForBasicErrors(basicUserError, availableBalance);
        if(basicErrors != null) return basicErrors;

        //swapQuantity already checked in checkForBasicErrors, so no need to assert swapQuantity
        BigDecimal exactSwapAmount = getExactSwapAmount(availableBalance, swapQuantity);
        volume = exactSwapAmount;

        Error error = new Error(model, userId, swap, "Minimum swap price must be at least 1 USD", bindingResult);

        if(swap.getFrom().equals("US Dollar")) {
            HistoryVolume historyVolumes = swapFrom(swap, error, swapQuantity, exactSwapAmount);
            history = historyVolumes.getHistory();
            volume = historyVolumes.getVolume();
        } else if(swap.getTo().equals("US Dollar")) {
            if(swapQuantity.compareTo(MINIMUM_SWAP_USD) < 0)
                return swapValidation.swapError(error, SwapErrorType.QUANTITY);

            CryptoNamePrice price = apiService.getCryptoPrice(swap.getFrom());
            history = mapToHistory.toUSDtoHistory(swap, price, exactSwapAmount);
            volume = volume.multiply(BigDecimal.valueOf(price.getPrice()));
        }
        else {
            error.setMessage("One or more of the currencies you selected are not valid.");
            List<CryptoNamePrice> prices = apiService.getCryptoPair(swap.getFrom(), swap.getTo());
            if(prices.size() < 2)
                return swapValidation.swapError(error, SwapErrorType.TO);

            BigDecimal fromPrice = new BigDecimal(String.valueOf(prices.getFirst().getPrice()))
                    .setScale(CRYPTO_PRECISION, RoundingMode.HALF_EVEN);
            volume = fromPrice.multiply(exactSwapAmount).setScale(2, RoundingMode.HALF_EVEN);

            if(volume.compareTo(MINIMUM_SWAP_USD) < 0) {
                error.setMessage("Minimum swap price must be at least 1 USD");
                return swapValidation.swapError(error, SwapErrorType.QUANTITY);
            }

            history = mapToHistory.toHistory(swap, prices, exactSwapAmount);
        }
        balanceRepository.updateTotalVolumeByAmountAndUserId(volume, userId);

        saveToHistory.saveHistory(new SaveHistory(history, swap, userId, volume, exactSwapAmount, Type.CRYPTO));
        return swapValidation.swapSuccessful(model, userId, swap, history);
    }
}
