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
    private static final int PRECISION = 8;
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

    private String validateQuantity(BigDecimal swapQuantity, Error error, Double price) {
        if((swapQuantity.multiply(BigDecimal.valueOf(price))).compareTo(MINIMUM_SWAP_USD) < 0)
            return swapValidation.swapError(error, SwapErrorType.QUANTITY);
        return null;
    }

    private HistoryVolume swapFrom(SwapDto swap, Error error, BigDecimal swapQuantity , BigDecimal exactSwapAmount) {
        if(validateQuantity(swapQuantity, error, 1.0) != null) return null;
        CryptoNamePrice price = apiService.getCryptoPrice(swap.getTo());
        History history = mapToHistory.fromUSDtoHistory(swap, price, exactSwapAmount);
        return new HistoryVolume(history, exactSwapAmount);
    }

    private HistoryVolume swapTo(SwapDto swap, Error error, BigDecimal swapQuantity , BigDecimal exactSwapAmount) {
        CryptoNamePrice price = apiService.getCryptoPrice(swap.getFrom());
        validateQuantity(swapQuantity, error, price.getPrice());
        History history = mapToHistory.toUSDtoHistory(swap, price, exactSwapAmount);
        return new HistoryVolume(history, exactSwapAmount);
    }

    private String swapCurrencyError(Error error, int size) {
        if(size < 2)
            return swapValidation.swapError(error, SwapErrorType.TO);
        return null;
    }

    private String swapVolumeError(Error error, BigDecimal volume) {
        if(volume.compareTo(MINIMUM_SWAP_USD) < 0) {
            error.setMessage("Minimum swap price must be at least 1 USD");
            return swapValidation.swapError(error, SwapErrorType.QUANTITY);
        }
        return null;
    }

    private HistoryVolume swapCrypto(SwapDto swap, Error error, BigDecimal exactSwapAmount) {
        error.setMessage("One or more of the currencies you selected are not valid.");
        List<CryptoNamePrice> prices = apiService.getCryptoPair(swap.getFrom(), swap.getTo());
        if(swapCurrencyError(error, prices.size()) != null)
            return null;

        BigDecimal fromPrice = new BigDecimal(String.valueOf(prices.getFirst().getPrice()))
                .setScale(PRECISION, RoundingMode.HALF_EVEN);
        BigDecimal volume = fromPrice.multiply(exactSwapAmount).setScale(2, RoundingMode.HALF_EVEN);

        if(swapVolumeError(error, volume) != null)
            return null;

        History history = mapToHistory.toHistory(swap, prices, exactSwapAmount);
        return new HistoryVolume(history, volume);
    }

    private HistoryVolume getHistoryVolume(SwapDto swap, Error error, BasicUserError basicUserError, BigDecimal availableBalance) {
        BigDecimal swapQuantity = parseQuantity(swap.getQuantity());
        String basicErrors = swapValidation.checkForBasicErrors(basicUserError, availableBalance);
        if(basicErrors != null) return null;
        BigDecimal exactSwapAmount = getExactSwapAmount(availableBalance, swapQuantity);

        if(swap.getFrom().equals(CurrencyConstant.USDName)) {
            return swapFrom(swap, error, swapQuantity, exactSwapAmount);
        } else if(swap.getTo().equals(CurrencyConstant.USDName)) {
            return swapTo(swap, error, swapQuantity, exactSwapAmount);
        } else {
            return swapCrypto(swap, error, exactSwapAmount);
        }
    }

    public String userSwap(Model model, SwapDto swap, Long userId, BindingResult bindingResult) {
        BigDecimal swapQuantity = parseQuantity(swap.getQuantity());
        BasicUserError basicUserError = new BasicUserError(model, swapQuantity, swap, userId, bindingResult);
        BigDecimal availableBalance = portfolioRepository.getQuantityBySymbolAndUserId(swap.getFrom(), userId);
        //swapQuantity already checked in checkForBasicErrors, so no need to assert swapQuantity
        Error error = new Error(model, userId, swap, "Minimum swap price must be at least 1 USD", bindingResult);

        HistoryVolume historyVolume = getHistoryVolume(swap, error, basicUserError, availableBalance);
        if(historyVolume == null) return "swap";
        BigDecimal exactSwapAmount = getExactSwapAmount(availableBalance, swapQuantity);
        balanceRepository.updateTotalVolumeByAmountAndUserId(historyVolume.getVolume(), userId);
        saveToHistory.saveHistory(new SaveHistory(historyVolume.getHistory(), swap, userId, historyVolume.getVolume(), exactSwapAmount, Type.CRYPTO));
        return swapValidation.swapSuccessful(model, userId, swap, historyVolume.getHistory());
    }
}
