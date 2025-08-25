package com.market.tradingbit.helpers;

import com.market.tradingbit.dtos.PortfolioDto;
import com.market.tradingbit.dtos.SuccessfulSwapDto;
import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.SwapErrorType;
import com.market.tradingbit.mappers.HistoryMapper;
import com.market.tradingbit.mappers.PortfolioMapper;
import com.market.tradingbit.models.*;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@AllArgsConstructor
public class SwapValidation {
    private final HistoryRepository historyRepository;
    private final PortfolioRepository portfolioRepository;
    private final CoinMarketCapService service;
    private final HistoryMapper historyMapper;
    private final MapToHistory mapToHistory;
    private final ApiService apiService;
    private final PortfolioMapper portfolioMapper;

    private static final int PRECISION = 8;
    private static final int CRYPTO_PRECISION = 8;
    private final double SLIPPAGE = 0.01;

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
        BigDecimal promisedFromPrice = parseQuantity(swap.getPromisedFromPrice());
        BigDecimal promisedToPrice = parseQuantity(swap.getPromisedToPrice());
        if (quantity == null) return "quantity_invalid";
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) return "quantity_zero_or_negative";
        if (swap.getFrom().equals(swap.getTo())) return "same_currency";
        if(notSufficientBalance(availableBalance, swapQuantity)) return "insufficient_balance";
        if(promisedFromPrice == null || promisedToPrice == null) return "invalid_promised_price";
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
            case "invalid_promised_price" -> bindingResult.addError(new FieldError("swap", "quantity", "Could not parse crypto price(s)"));
        }
    }

    private void populateModel(Model model, Long userId) {
        List<CryptoSymbolPrice> latestListings = service.getAllCryptoSymbolPrices();
        model.addAttribute("swapItems", latestListings);

        List<Portfolio> portfolioList = portfolioRepository.getCryptoPortfolioByUserId(userId);
        List<PortfolioDto> portfolioDto = portfolioMapper.toPortfolioDto(portfolioList);
        portfolioDto.forEach(portfolio -> {
            if ("USD".equalsIgnoreCase(portfolio.getSymbol())) {
                portfolio.setPrice("1.0");
            } else {
                latestListings.stream()
                        .filter(crypto -> crypto.getSymbol().equalsIgnoreCase(portfolio.getSymbol()))
                        .findFirst()
                        .ifPresentOrElse(
                                crypto -> portfolio.setPrice(crypto.getPrice().toString()),
                                () -> {
                                    portfolio.setPrice("0.00");
                                    System.out.println("Price not found for symbol: " + portfolio.getSymbol());
                                }
                        );
            }
        });
        model.addAttribute("lastUpdated", new SimpleDateFormat("MMM dd, HH:mm:ss").format(new Date()));
        model.addAttribute("userItems", portfolioDto);
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

    private String swapFromError(Error fromError) {
        fromError.getBindingResult().addError(new FieldError(
                "swap", "from", fromError.getMessage()
        ));
        return returnBindingResult(fromError.getModel(), fromError.getUserId(), fromError.getSwap());
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

    private String swapError(Error error, SwapErrorType field) {
        switch (field) {
            case FROM -> {
                return swapFromError(error);
            }
            case TO -> {
                return swapToError(error);
            }
            case QUANTITY -> {
                return swapQuantityError(error);
            }
        }
        return null;
    }

    private String validateQuantity(BigDecimal swapQuantity, Error error, Double price) {
        if((swapQuantity.multiply(BigDecimal.valueOf(price))).compareTo(MINIMUM_SWAP_USD) < 0)
            return swapError(error, SwapErrorType.QUANTITY);
        return null;
    }

    private String swapCurrencyError(Error error, int size) {
        if(size < 2)
            return swapError(error, SwapErrorType.TO);
        return null;
    }

    private void slippageError(Error error) {
        error.setMessage("Slippage exceeded. Please refresh for newer prices and try again");
        swapError(error, SwapErrorType.FROM);
    }

    private String swapVolumeError(Error error, BigDecimal volume) {
        if(volume.compareTo(MINIMUM_SWAP_USD) < 0) {
            error.setMessage("Minimum swap price must be at least 1 USD");
            return swapError(error, SwapErrorType.QUANTITY);
        }
        return null;
    }

    public String checkForBasicErrors(BasicUserError error, BigDecimal availableBalance) {
        validateBasicFields(error.getSwap(), error.getBindingResult(), availableBalance, error.getSwapQuantity());
        if(error.getBindingResult().hasErrors())
            return returnBindingResult(error.getModel(), error.getUserId(), error.getSwap());
        return null;
    }

    public HistoryVolume swapFrom(SwapDto swap, Error error, BigDecimal swapQuantity , BigDecimal exactSwapAmount) {
        if(validateQuantity(swapQuantity, error, 1.0) != null) return null;
        CryptoNamePrice price = apiService.getCryptoPrice(swap.getTo());
        if(price.getPrice() * (1 + SLIPPAGE) < Double.parseDouble(swap.getPromisedToPrice())) {
            slippageError(error);
            return null;
        }
        History history = mapToHistory.fromUSDtoHistory(swap, price, exactSwapAmount);
        return new HistoryVolume(history, exactSwapAmount);
    }

    public HistoryVolume swapTo(SwapDto swap, Error error, BigDecimal swapQuantity , BigDecimal exactSwapAmount) {
        CryptoNamePrice price = apiService.getCryptoPrice(swap.getFrom());
        if(validateQuantity(swapQuantity, error, price.getPrice()) != null) return null;
        if(price.getPrice() * (1 + SLIPPAGE) < Double.parseDouble(swap.getPromisedFromPrice())) {
            slippageError(error);
            return null;
        }
        History history = mapToHistory.toUSDtoHistory(swap, price, exactSwapAmount);
        return new HistoryVolume(history, exactSwapAmount.multiply(BigDecimal.valueOf(price.getPrice())));
    }

    public HistoryVolume swapCrypto(SwapDto swap, Error error, BigDecimal exactSwapAmount) {
        error.setMessage("One or more of the currencies you selected are not valid.");
        List<CryptoNamePrice> prices = apiService.getCryptoPair(swap.getFrom(), swap.getTo());
        if(swapCurrencyError(error, prices.size()) != null)
            return null;

        BigDecimal fromPrice = new BigDecimal(String.valueOf(prices.get(0).getPrice()))
                .setScale(PRECISION, RoundingMode.HALF_EVEN);
        BigDecimal volume = fromPrice.multiply(exactSwapAmount).setScale(2, RoundingMode.HALF_EVEN);

        if(swapVolumeError(error, volume) != null)
            return null;

        double promisedRatio = Double.parseDouble(swap.getPromisedFromPrice()) / Double.parseDouble(swap.getPromisedToPrice());
        double actualRatio = prices.get(0).getPrice() / prices.get(prices.size()-1).getPrice();

        if(Math.abs(promisedRatio - actualRatio) > SLIPPAGE) {
            slippageError(error);
            return null;
        }

        History history = mapToHistory.toHistory(swap, prices, exactSwapAmount);
        return new HistoryVolume(history, volume);
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
        model.addAttribute("lastUpdated", new SimpleDateFormat("MMM dd, HH:mm:ss").format(new Date()));
        return "swap";
    }
}
