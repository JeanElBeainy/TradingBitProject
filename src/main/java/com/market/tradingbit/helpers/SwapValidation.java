package com.market.tradingbit.helpers;

import com.market.tradingbit.dtos.SuccessfulSwapDto;
import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.SwapErrorType;
import com.market.tradingbit.mappers.HistoryMapper;
import com.market.tradingbit.models.BasicUserError;
import com.market.tradingbit.models.CryptoNameSymbol;
import com.market.tradingbit.models.Error;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@AllArgsConstructor
public class SwapValidation {
    private final HistoryRepository historyRepository;
    private final PortfolioRepository portfolioRepository;
    private final CoinMarketCapService service;
    private final HistoryMapper historyMapper;
    private static final int CRYPTO_PRECISION = 8;
    private final BigDecimal MINIMUM_SWAP_USD = new BigDecimal("1.00");


    public static BigDecimal parseQuantity(String quantityStr) {
        try {
            return new BigDecimal(quantityStr.trim()).setScale(CRYPTO_PRECISION, RoundingMode.HALF_EVEN);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private static boolean notSufficientBalance(BigDecimal availableBalance, BigDecimal requiredAmount) {
        if (availableBalance == null) return true;
        BigDecimal difference = availableBalance.subtract(requiredAmount);
        return difference.compareTo(BigDecimal.ZERO) < 0;
    }

    private static String getValidationError(SwapDto swap, BigDecimal availableBalance, BigDecimal swapQuantity) {
        if (swap.getFrom() == null || swap.getFrom().isEmpty()) return "from_empty";
        if (swap.getTo() == null || swap.getTo().isEmpty()) return "to_empty";

        BigDecimal quantity = parseQuantity(swap.getQuantity());
        if (quantity == null) return "quantity_invalid";
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) return "quantity_zero_or_negative";
        if (swap.getFrom().equals(swap.getTo())) return "same_currency";
        if(notSufficientBalance(availableBalance, swapQuantity)) return "insufficient_balance";
        return "valid";
    }

    public static void validateBasicFields(SwapDto swap, BindingResult bindingResult, BigDecimal availableBalance, BigDecimal swapQuantity) {
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

    private void populateModel(Model model, Long userId) {
        List<Portfolio> portfolioList = portfolioRepository.getCryptoPortfolioByUserId(userId);
        model.addAttribute("userItems", portfolioList);

        List<CryptoNameSymbol> latestListings = service.getLatestNameAndSymbol();
        model.addAttribute("swapItems", latestListings);
    }

    public void populateModelToUser(Model model, Long userId) {
        populateModel(model, userId);
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

    private String swapToError(Error toError) {
        toError.getBindingResult().addError(new FieldError(
                "swap", "to", toError.getMessage()
        ));
        return returnBindingResult(toError.getModel(), toError.getUserId(), toError.getSwap());
    }

    public String checkForBasicErrors(BasicUserError error, BigDecimal availableBalance) {
        validateBasicFields(error.getSwap(), error.getBindingResult(), availableBalance, error.getSwapQuantity());
        if(error.getBindingResult().hasErrors())
            return returnBindingResult(error.getModel(), error.getUserId(), error.getSwap());
        return null;
    }

    private String swapQuantityError(Error quantityError) {
        quantityError.getBindingResult().addError(new FieldError(
                "swap", "quantity", quantityError.getMessage()
        ));
        return returnBindingResult(quantityError.getModel(), quantityError.getUserId(), quantityError.getSwap());
    }

    public String swapError(Error error, SwapErrorType field) {
        switch (field) {
            case SwapErrorType.TO -> {
                return swapToError(error);
            }
            case SwapErrorType.QUANTITY -> {
                return swapQuantityError(error);
            }
        }
        return null;
    }

    public String validateQuantity(BigDecimal swapQuantity, Error error, Double price) {
        if((swapQuantity.multiply(BigDecimal.valueOf(price))).compareTo(MINIMUM_SWAP_USD) < 0)
            return swapError(error, SwapErrorType.QUANTITY);
        return null;
    }

    public String swapCurrencyError(Error error, int size) {
        if(size < 2)
            return swapError(error, SwapErrorType.TO);
        return null;
    }

    public String swapVolumeError(Error error, BigDecimal volume) {
        if(volume.compareTo(MINIMUM_SWAP_USD) < 0) {
            error.setMessage("Minimum swap price must be at least 1 USD");
            return swapError(error, SwapErrorType.QUANTITY);
        }
        return null;
    }

    public String swapSuccessful(Model model, Long userId, SwapDto swap, History history) {
        populateModel(model, userId);
        model.addAttribute("success", true);
        model.addAttribute("swap", swapToNull(swap));
        SuccessfulSwapDto successfulSwap = historyMapper.toSuccessfulSwapDto(history);
        model.addAttribute("successfulSwap", successfulSwap);
        successfulSwap.setFromSymbol(history.getFromSymbol());
        successfulSwap.setToSymbol(history.getToSymbol());
        model.addAttribute("history", historyRepository.findTop3ByUserIdOrderByIdDesc(userId));
        return "swap";
    }
}
