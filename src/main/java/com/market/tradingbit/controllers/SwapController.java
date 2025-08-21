package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.HistoryDto;
import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.mappers.HistoryMapper;
import com.market.tradingbit.models.*;
import com.market.tradingbit.models.Error;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/swap")
public class SwapController {

    private final CoinMarketCapService service;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final HistoryMapper historyMapper;
    private final BalanceRepository balanceRepository;
    private final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.001");
    private final BigDecimal MINIMUM_SWAP_USD = new BigDecimal("1.00");
    private final BigDecimal TOLERANCE = new BigDecimal("0.00000001");
    private static final int CRYPTO_PRECISION = 8;

    private void populateModel(Model model, Long userId) {
        List<Portfolio> portfolioList = portfolioRepository.getCryptoPortfolioByUserId(userId);
        model.addAttribute("userItems", portfolioList);

        List<CryptoNameSymbol> latestListings = service.getLatestNameAndSymbol();
        model.addAttribute("swapItems", latestListings);
    }

    private SwapDto swapToNull(SwapDto swap) {
        swap.setFrom(null);
        swap.setTo(null);
        swap.setQuantity(null);
        return swap;
    }

    private String returnBindingResult(Model model, Long userId, SwapDto swap) {
        populateModel(model, userId);
        model.addAttribute("swap", swapToNull(swap));
        model.addAttribute("history", historyRepository.findTop3ByUserIdOrderByIdDesc(userId));
        return "swap";
    }

    private BigDecimal parseQuantity(String quantityStr) {
        try {
            return new BigDecimal(quantityStr.trim()).setScale(CRYPTO_PRECISION, RoundingMode.HALF_EVEN);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private boolean notSufficientBalance(BigDecimal availableBalance, BigDecimal requiredAmount) {
        if (availableBalance == null) return true;
        BigDecimal difference = availableBalance.subtract(requiredAmount);
        return difference.compareTo(BigDecimal.ZERO) < 0;
    }

    private String getValidationError(SwapDto swap, BigDecimal availableBalance, BigDecimal swapQuantity) {
        if (swap.getFrom() == null || swap.getFrom().isEmpty()) return "from_empty";
        if (swap.getTo() == null || swap.getTo().isEmpty()) return "to_empty";

        BigDecimal quantity = parseQuantity(swap.getQuantity());
        if (quantity == null) return "quantity_invalid";
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) return "quantity_zero_or_negative";
        if (swap.getFrom().equals(swap.getTo())) return "same_currency";
        if(notSufficientBalance(availableBalance, swapQuantity)) return "insufficient_balance";
        return "valid";
    }

    private void validateBasicFields(SwapDto swap, BindingResult bindingResult, BigDecimal availableBalance, BigDecimal swapQuantity) {
        String validationError = getValidationError(swap, availableBalance, swapQuantity);
        switch (validationError) {
            case "from_empty" -> bindingResult.addError(new FieldError("swap", "from", "Please select a valid currency that you own"));
            case "to_empty" -> bindingResult.addError(new FieldError("swap", "to", "Please select a valid currency to swap"));
            case "quantity_invalid" -> bindingResult.addError(new FieldError("swap", "quantity", "Quantity is either empty or not a number"));
            case "quantity_zero_or_negative" -> bindingResult.addError(new FieldError("swap", "quantity", "Quantity cannot be less than or equal to zero"));
            case "same_currency" -> bindingResult.addError(new FieldError("swap", "to", "You cannot swap to the same currency you are swapping from"));
            case "insufficient_balance" -> bindingResult.addError(new FieldError("swap", "quantity", "You do not have enough "+ swap.getFrom() + " to perform this swap"));

        }
    }

    private BigDecimal getExactSwapAmount(BigDecimal availableBalance, BigDecimal requestedAmount) {
        BigDecimal difference = requestedAmount.subtract(availableBalance);
        if (difference.compareTo(BigDecimal.ZERO) > 0 && difference.compareTo(TOLERANCE) <= 0)
            return availableBalance;
        return requestedAmount;
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

    private String swapToError(Error toError) {
        toError.getBindingResult().addError(new FieldError(
                "swap", "to", toError.getMessage()
        ));
        return returnBindingResult(toError.getModel(), toError.getUserId(), toError.getSwap());
    }

    private String swapQuantityError(Error quantityError) {
        quantityError.getBindingResult().addError(new FieldError(
                "swap", "quantity", quantityError.getMessage()
        ));
        return returnBindingResult(quantityError.getModel(), quantityError.getUserId(), quantityError.getSwap());
    }

    private History fromUSDtoHistory(SwapDto swap, CryptoNamePrice price, BigDecimal exactQuantity) {
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

    private History toUSDtoHistory(SwapDto swap, CryptoNamePrice price, BigDecimal exactQuantity) {
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

    private History toHistory(SwapDto swap, List<CryptoNamePrice> prices, BigDecimal exactQuantity) {
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

    private void saveHistory(SaveHistory saveHistory) {
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

    //Does not contain queries if successful.
    private String checkForBasicErrors(BasicUserError error, BigDecimal availableBalance) {
        validateBasicFields(error.getSwap(), error.getBindingResult(), availableBalance, error.getSwapQuantity());
        if(error.getBindingResult().hasErrors())
            return returnBindingResult(error.getModel(), error.getUserId(), error.getSwap());
        return null;
    }

    @GetMapping("/crypto")
    //3 Queries: userRepository, portfolioRepository, historyRepository (ALL CRUCIAL)
    public String cryptoSwap(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        populateModel(model, user.getId());
        model.addAttribute("swap", new SwapDto());
        model.addAttribute("success", false);
        List<History> history = historyRepository.findTop3ByUserIdOrderByIdDesc(user.getId());
        model.addAttribute("history", history);
        return "swap";
    }

    @PostMapping("/crypto")
    public String cryptoSwap(Model model, @Valid @ModelAttribute("swap") SwapDto swap, Principal principal, BindingResult bindingResult) {
        if(principal == null) return "redirect:/login";
        System.out.println("Post Mapping:");
        User user = userRepository.findByEmail(principal.getName());
        Long userId = user.getId();
        BigDecimal volume;
        History history;

        BigDecimal swapQuantity = parseQuantity(swap.getQuantity());
        BigDecimal availableBalance = portfolioRepository.getQuantityBySymbolAndUserId(swap.getFrom(), userId);
        String basicErrors = checkForBasicErrors(new BasicUserError(model, swapQuantity, swap, userId, bindingResult), availableBalance);
        if(basicErrors != null) return basicErrors;

        //swapQuantity already checked in checkForBasicErrors, so no need to assert swapQuantity
        BigDecimal exactSwapAmount = getExactSwapAmount(availableBalance, swapQuantity);
        volume = exactSwapAmount;

        if(swap.getFrom().equals("US Dollar")) {
            if(swapQuantity.compareTo(MINIMUM_SWAP_USD) < 0)
                return swapQuantityError(new Error(model, userId, swap, "Minimum swap price must be at least 1 USD", bindingResult));
            CryptoNamePrice price = service.getCryptoNameBySymbol(swap.getTo());
            history = fromUSDtoHistory(swap, price, exactSwapAmount);
        } else if(swap.getTo().equals("US Dollar")) {
            if(swapQuantity.compareTo(MINIMUM_SWAP_USD) < 0)
                return swapQuantityError(new Error(model, userId, swap, "Minimum swap price must be at least 1 USD", bindingResult));
            CryptoNamePrice price = service.getCryptoNameBySymbol(swap.getFrom());
            history = toUSDtoHistory(swap, price, exactSwapAmount);
            volume = volume.multiply(BigDecimal.valueOf(price.getPrice()));
        }
        else {
            List<CryptoNamePrice> prices = service.getPricesBySymbols(swap.getFrom(), swap.getTo());
            if(prices.size() < 2)
                return swapToError(new Error(model, userId, swap, "One or more of the currencies you selected are not valid.", bindingResult));

            BigDecimal fromPrice = new BigDecimal(String.valueOf(prices.getFirst().getPrice()))
                    .setScale(CRYPTO_PRECISION, RoundingMode.HALF_EVEN);
            volume = fromPrice.multiply(exactSwapAmount).setScale(2, RoundingMode.HALF_EVEN);

            if(volume.compareTo(MINIMUM_SWAP_USD) < 0)
                return swapQuantityError(new Error(model, userId, swap, "Minimum swap price must be at least 1 USD", bindingResult));

            history = toHistory(swap, prices, exactSwapAmount);
        }
        balanceRepository.updateTotalVolumeByAmountAndUserId(volume, userId);

        if(bindingResult.hasErrors())
            return returnBindingResult(model, userId, swap);

        saveHistory(new SaveHistory(history, swap, userId, volume, exactSwapAmount));
        if(swap.getFrom().equals("US Dollar") || swap.getTo().equals("US Dollar"))
            balanceRepository.updateUSDBalanceByAmountAndUserId(
                    portfolioRepository.getQuantityBySymbolAndUserId("US Dollar", user.getId()),
                    user.getId());
        populateModel(model, userId);
        model.addAttribute("success", true);
        model.addAttribute("swap", swapToNull(swap));
        model.addAttribute("successfulSwap", historyMapper.toSuccessfulSwapDto(history));
        model.addAttribute("history", historyRepository.findTop3ByUserIdOrderByIdDesc(userId));
        return "swap";
    }

    @GetMapping("/stock")
    public String stockSwap(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        List<Portfolio> portfolioList = portfolioRepository.getStockPortfolioByUserId(userRepository.findByEmail(principal.getName()).getId());
        model.addAttribute("userItems", portfolioList);
        return "swap";
    }
}