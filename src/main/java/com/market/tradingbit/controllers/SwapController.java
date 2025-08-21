package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.*;
import com.market.tradingbit.helpers.ApiService;
import com.market.tradingbit.helpers.MapToHistory;
import com.market.tradingbit.helpers.SaveToHistory;
import com.market.tradingbit.helpers.SwapValidation;
import com.market.tradingbit.models.*;
import com.market.tradingbit.models.Error;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.List;

import static com.market.tradingbit.helpers.SwapValidation.*;

@Controller
@AllArgsConstructor
@RequestMapping("/swap")
public class SwapController {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final BalanceRepository balanceRepository;
    private final SaveToHistory saveToHistory;
    private final MapToHistory mapToHistory;
    private final SwapValidation swapValidation;
    private final ApiService apiService;
    private final BigDecimal MINIMUM_SWAP_USD = new BigDecimal("1.00");
    private final BigDecimal TOLERANCE = new BigDecimal("0.00000001");
    private static final int CRYPTO_PRECISION = 8;

    private BigDecimal getExactSwapAmount(BigDecimal availableBalance, BigDecimal requestedAmount) {
        BigDecimal difference = requestedAmount.subtract(availableBalance);
        if (difference.compareTo(BigDecimal.ZERO) > 0 && difference.compareTo(TOLERANCE) <= 0)
            return availableBalance;
        return requestedAmount;
    }

    @GetMapping("/crypto")
    //3 Queries: userRepository, portfolioRepository, historyRepository (ALL CRUCIAL)
    public String cryptoSwap(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        swapValidation.populateModelToUser(model, user.getId());
        model.addAttribute("swap", new SwapDto());
        model.addAttribute("success", false);
        List<History> history = historyRepository.findTop3ByUserIdOrderByIdDesc(user.getId());
        model.addAttribute("history", history);
        return "swap";
    }

    private String userSwap(Model model, SwapDto swap, Long userId, BindingResult bindingResult) {
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
            CryptoNamePrice price = apiService.getCryptoPrice(swap.getTo());

            if((swapQuantity.multiply(BigDecimal.valueOf(price.getPrice()))).compareTo(MINIMUM_SWAP_USD) < 0)
                return swapValidation.swapError(error, SwapErrorType.QUANTITY);

            history = mapToHistory.fromUSDtoHistory(swap, price, exactSwapAmount);
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

    @PostMapping("/crypto")
    public String cryptoSwap(Model model, @Valid @ModelAttribute("swap") SwapDto swap, Principal principal, BindingResult bindingResult) {
        if(principal == null) return "redirect:/login";
        System.out.println("Post Mapping:");
        User user = userRepository.findByEmail(principal.getName());
        return userSwap(model, swap, user.getId(), bindingResult);
    }

    @GetMapping("/stock")
    public String stockSwap(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        List<Portfolio> portfolioList = portfolioRepository.getStockPortfolioByUserId(userRepository.findByEmail(principal.getName()).getId());
        model.addAttribute("userItems", portfolioList);
        return "swap";
    }
}