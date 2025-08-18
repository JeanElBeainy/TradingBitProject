package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.SuccessfulSwapDto;
import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.models.CryptoNamePrice;
import com.market.tradingbit.models.CryptoNameSymbol;
import com.market.tradingbit.models.QuantityError;
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

    private void appendToRepository(SwapDto swap, Long userId, String toName, float quantityPriceTo) {
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

        portfolioRepository.updatePortfolioQuantity(swap.getQuantity()*-1, swap.getFrom(), userId);

        if(portfolioRepository.getItemBySymbolAndUserId(swap.getFrom(), userId).getQuantity() == 0)
            portfolioRepository.deleteById(portfolioRepository.getItemBySymbolAndUserId(swap.getFrom(), userId).getId());

    }

    private String swapQuantityError(QuantityError quantityError) {
        quantityError.getBindingResult().addError(new FieldError(
                "swap", "quantity", quantityError.getMessage()
        ));
        return returnBindingResult(quantityError.getModel(), quantityError.getUserId(), quantityError.getSwap());
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

    //TODO: try to make swap quantity string instead of float
    @PostMapping("/crypto")
    public String cryptoSwap(Model model, @Valid @ModelAttribute("swap") SwapDto swap, Principal principal, BindingResult bindingResult) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        Long userId = user.getId();

        if(Float.isNaN(swap.getQuantity()))
            return swapQuantityError(new QuantityError(model, userId, swap, "Quantity must be a valid number", bindingResult));

        History history = new History();
        history.setUserId(userId);
        history.setFromQuantity(swap.getQuantity());
        history.setToSymbol(swap.getTo());
        history.setFromSymbol(swap.getFrom());

        validateBasicFields(swap, bindingResult);
        if(bindingResult.hasErrors())
            return returnBindingResult(model, userId, swap);

        if(history.getFromSymbol().equals("US Dollar")) {
            if(swap.getQuantity() < 1)
                return swapQuantityError(new QuantityError(model, userId, swap, "Minimum swap price must be at least 1 USD",  bindingResult));
            if(swap.getQuantity() > portfolioRepository.getQuantityBySymbolAndUserId(swap.getFrom(), user.getId()))
                return swapQuantityError(new QuantityError(model, userId, swap, "You do not have enough USD to perform this swap",  bindingResult));
            CryptoNamePrice price = service.getCryptoNameBySymbol(swap.getTo());
            history.setFromName("US Dollar Balance");
            history.setFromQuantity(swap.getQuantity());
            history.setFromPrice(1);
            history.setToPrice(price.getPrice());
            history.setToName(price.getName());
        } else { //TODO: return symbol name
            if(swap.getQuantity() > portfolioRepository.getQuantityBySymbolAndUserId(swap.getFrom(), user.getId()))
                return swapQuantityError(new QuantityError(model, userId, swap, "You do not have enough " + swap.getFrom() + " to perform this swap", bindingResult));

            List<CryptoNamePrice> prices = service.getPricesBySymbols(swap.getFrom(), swap.getTo());
            if(prices.size() < 2)
                bindingResult.addError(new FieldError(
                        "swap", "to", "One or more of the currencies you selected are not valid."
                ));

            history.setFromPrice((float) (prices.getFirst().getPrice() * swap.getQuantity()));
            if(history.getFromPrice() < 1)
                return swapQuantityError(new QuantityError(model, userId, swap, "Minimum swap price must be at least 1 USD",  bindingResult));

            //NOTE: CryptoNamePrice will return: FROM-name and TO-name-price
            history.setFromName("Solana");
            history.setToName("USDC");
            history.setToPrice(prices.getLast().getPrice());
        }
        if(bindingResult.hasErrors())
            return returnBindingResult(model, userId, swap);

        float toQuantity = (float) (history.getFromPrice() / history.getToPrice());
        float fee = toQuantity * FEE_PERCENTAGE;
        history.setToQuantity(toQuantity - fee);

        appendToRepository(swap, userId, history.getToName(), history.getToQuantity());
        populateModel(model, userId);

        historyRepository.save(history);

        model.addAttribute("success", true);

        SuccessfulSwapDto successfulSwapDto = SuccessfulSwapDto.builder()
                .from(swap.getFrom())
                .to(swap.getTo())
                .fromQuantity(history.getFromQuantity())
                .toQuantity(history.getToQuantity())
                .fee(fee)
                .build();

        history.setFee(fee);
        history.setVolume((float) history.getFromPrice());
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
