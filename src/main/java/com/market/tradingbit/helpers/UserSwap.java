package com.market.tradingbit.helpers;

import com.market.tradingbit.dtos.SwapDto;
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

import static com.market.tradingbit.helpers.SwapValidation.parseQuantity;

@Component
@AllArgsConstructor
public class UserSwap {
    private final PortfolioRepository portfolioRepository;
    private final SwapValidation swapValidation;
    private final BalanceRepository balanceRepository;
    private final SaveToHistory saveToHistory;
    private final BigDecimal TOLERANCE = new BigDecimal("0.00000001");

    private BigDecimal getExactSwapAmount(BigDecimal availableBalance, BigDecimal requestedAmount) {
        BigDecimal difference = requestedAmount.subtract(availableBalance);
        if (difference.compareTo(BigDecimal.ZERO) > 0 && difference.compareTo(TOLERANCE) <= 0)
            return availableBalance;
        return requestedAmount;
    }

    private HistoryVolume getHistoryVolume(SwapDto swap, Error error, BasicUserError basicUserError, BigDecimal availableBalance) {
        BigDecimal swapQuantity = parseQuantity(swap.getQuantity());
        String basicErrors = swapValidation.checkForBasicErrors(basicUserError, availableBalance);
        if(basicErrors != null) return null;
        BigDecimal exactSwapAmount = getExactSwapAmount(availableBalance, swapQuantity);

        if(swap.getFrom().equals(CurrencyConstant.USDName)) {
            return swapValidation.swapFrom(swap, error, swapQuantity, exactSwapAmount);
        } else if(swap.getTo().equals(CurrencyConstant.USDName)) {
            return swapValidation.swapTo(swap, error, swapQuantity, exactSwapAmount);
        } else {
            return swapValidation.swapCrypto(swap, error, exactSwapAmount);
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
