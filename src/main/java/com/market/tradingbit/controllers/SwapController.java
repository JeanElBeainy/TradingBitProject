package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.HistoryDto;
import com.market.tradingbit.dtos.SuccessfulSwapDto;
import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.mappers.HistoryMapper;
import com.market.tradingbit.models.CryptoNamePrice;
import com.market.tradingbit.models.CryptoNameSymbol;
import com.market.tradingbit.models.Error;
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

    private final float FEE_PERCENTAGE = 0.001f;

    private void populateModel(Model model, Long userId) {
        List<Portfolio> portfolioList = portfolioRepository.getCryptoPortfolioByUserId(userId);
        model.addAttribute("userItems", portfolioList);

        List<CryptoNameSymbol> latestListings = service.getLatestNameAndSymbol();
        model.addAttribute("swapItems", latestListings);
    }

    private String returnBindingResult(Model model, Long userId, SwapDto swap) {
        populateModel(model, userId);
        model.addAttribute("swap", swap);
        return "swap";
    }

    private void validateBasicFields(SwapDto swap, BindingResult bindingResult) {
        if(swap.getFrom() == null || swap.getFrom().isEmpty())
            bindingResult.addError(new FieldError("swap", "from", "Please select a valid currency that you own."));
        if(swap.getTo() == null || swap.getTo().isEmpty())
            bindingResult.addError(new FieldError("swap", "to", "Please select a valid currency to swap."));
        if(swap.getQuantity() <= 0)
            bindingResult.addError(new FieldError("swap", "quantity", "Quantity cannot be less than or equal to zero."));
        if(bindingResult.hasErrors())
            return;
        if(swap.getFrom().equals(swap.getTo()))
            bindingResult.addError(new FieldError("swap", "to", "You cannot swap to the same currency you are swapping from."));
    }

    private void appendToRepository(SwapDto swap, Long userId, String toName, BigDecimal quantityPriceTo) {
        if(portfolioRepository.getItemBySymbolAndUserId(swap.getTo(), userId) == null) {
            portfolioRepository.save(Portfolio.builder()
                    .symbol(swap.getTo())
                    .purchaseType(Type.CRYPTO)
                    .userId(userId)
                    .name(toName)
                    .quantity(quantityPriceTo)
                    .build());
        } else
            portfolioRepository.updatePortfolioQuantity(quantityPriceTo, swap.getTo(), userId);

        portfolioRepository.updatePortfolioQuantity(BigDecimal.valueOf(swap.getQuantity()*-1), swap.getFrom(), userId);
        BigDecimal quantity = portfolioRepository.getItemBySymbolAndUserId(swap.getFrom(), userId).getQuantity();
        if(quantity.equals(BigDecimal.ZERO))
            portfolioRepository.deleteById(portfolioRepository.getItemBySymbolAndUserId(swap.getFrom(), userId).getId());
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

    private String swapSuccessful(SuccessfulSwapDto successfulSwapDto, Model model) {

        return "swap";
    }

    @GetMapping("/crypto")
    public String cryptoSwap(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        populateModel(model, user.getId());
        model.addAttribute("swap", new SwapDto());
        model.addAttribute("success", false);
        return "swap";
    }
    private void setId(History history, Long userId) {
        history.setId(userId);
    }

    //TODO: try to make swap quantity string instead of float
    @PostMapping("/crypto")
    public String cryptoSwap(Model model, @Valid @ModelAttribute("swap") SwapDto swap, Principal principal, BindingResult bindingResult) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        Long userId = user.getId();

        if(Float.isNaN(swap.getQuantity()))
            return swapQuantityError(new Error(model, userId, swap, "Quantity must be a valid number", bindingResult));

        History history = new History();
//        history.setFromQuantity(BigDecimal.valueOf(swap.getQuantity()));
//        history.setToSymbol(swap.getTo());

        validateBasicFields(swap, bindingResult);
        if(bindingResult.hasErrors())
            return returnBindingResult(model, userId, swap);

        if(swap.getFrom().equals("US Dollar")) {
            if(swap.getQuantity() < 1)
                return swapQuantityError(new Error(model, userId, swap, "Minimum swap price must be at least 1 USD",  bindingResult));
            if(swap.getQuantity() > portfolioRepository.getQuantityBySymbolAndUserId(swap.getFrom(), userId))
                return swapQuantityError(new Error(model, userId, swap, "You do not have enough USD to perform this swap",  bindingResult));

            CryptoNamePrice price = service.getCryptoNameBySymbol(swap.getTo());
            //create a class with these and save them using historyMapper.

            HistoryDto fromUSD = new HistoryDto("US Dollar",
                    "US Dollar Balance",
                    BigDecimal.valueOf(swap.getQuantity()),
                    1,
                    swap.getTo(),
                    price.getName(),
                    price.getPrice());
            history = historyMapper.toHistory(fromUSD);
        } else {
            List<CryptoNamePrice> prices = service.getPricesBySymbols(swap.getFrom(), swap.getTo());

            if(swap.getQuantity() > portfolioRepository.getQuantityBySymbolAndUserId(swap.getFrom(), userId))
                return swapQuantityError(new Error(model, userId, swap, "You do not have enough " + swap.getFrom() + " to perform this swap", bindingResult));
            if(prices.size() < 2)
                return swapToError(new Error(model, userId, swap, "One or more of the currencies you selected are not valid.", bindingResult));

            history.setFromPrice(prices.getFirst().getPrice());
            history.setVolume((float) (prices.getFirst().getPrice() * swap.getQuantity()));
            if(history.getVolume() < 1)
                return swapQuantityError(new Error(model, userId, swap, "Minimum swap price must be at least 1 USD",  bindingResult));

            //NOTE: CryptoNamePrice will return: FROM-name and TO-name-price
            history.setFromName(prices.getFirst().getName());
            history.setToName(prices.getLast().getName());
            history.setToPrice(prices.getLast().getPrice());
        }
        if(bindingResult.hasErrors())
            return returnBindingResult(model, userId, swap);

        BigDecimal toQuantity = BigDecimal.valueOf((history.getFromPrice() * swap.getQuantity() / history.getToPrice()));
        BigDecimal fee = toQuantity.multiply(BigDecimal.valueOf(FEE_PERCENTAGE));
        history.setToQuantity(toQuantity.subtract(fee));

        appendToRepository(swap, userId, history.getToName(), history.getToQuantity());
        populateModel(model, userId);
        history.setFee(fee);
        history.setUserId(userId);

        historyRepository.save(history);

        model.addAttribute("success", true);

        SuccessfulSwapDto successfulSwapDto = SuccessfulSwapDto.builder() //TODO: Try fromHistory()
                .from(history.getFromSymbol())
                .to(history.getToSymbol())
                .fromQuantity(history.getFromQuantity().setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString())
                .toQuantity(history.getToQuantity().setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString())
                .fee(history.getFee().setScale(8, RoundingMode.HALF_UP).toPlainString())
                .build();

        System.out.println(history);
        model.addAttribute("successfulSwap", successfulSwapDto);
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
