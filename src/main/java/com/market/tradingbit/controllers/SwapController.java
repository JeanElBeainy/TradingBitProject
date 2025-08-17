package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.models.CryptoNamePrice;
import com.market.tradingbit.models.CryptoNameSymbol;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import jakarta.transaction.Transactional;
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

    private void populateModel(Model model, User user) {
        List<Portfolio> portfolioList = portfolioRepository.getCryptoPortfolioByUserId(user.getId());
        model.addAttribute("userItems", portfolioList);

        List<CryptoNameSymbol> latestListings = service.getLatestNameAndSymbol();
        model.addAttribute("swapItems", latestListings);
    }

    private String returnBindingResult(Model model, User user, SwapDto swap) {
        populateModel(model, user);
        model.addAttribute("swap", swap);
        return "swap";
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

    @GetMapping("/crypto")
    public String cryptoSwap(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user;
        try {
            user = userRepository.findByEmail(principal.getName());
        } catch (Exception e) {
            System.out.println("Exception with swap: " + e.getMessage());
            return "swap";
        }
        populateModel(model, user);
        model.addAttribute("swap", new SwapDto());
        return "swap";
    }

    @PostMapping("/crypto")
    public String cryptoSwap(Model model, @Valid @ModelAttribute("swap") SwapDto swap, Principal principal, BindingResult bindingResult) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());

        Double toPrice;
        String toName;
        float quantityPriceFrom = swap.getQuantity();
        float feePercentage = 0.001f;

        if(swap.getFrom() == null || swap.getFrom().isEmpty())
            bindingResult.addError(new FieldError("swap", "from", "Please select a valid currency that you own."));
        if(swap.getTo() == null || swap.getTo().isEmpty())
            bindingResult.addError(new FieldError("swap", "to", "Please select a valid currency to swap."));
        if(swap.getQuantity() <= 0)
            bindingResult.addError(new FieldError("swap", "quantity", "Quantity cannot be less than or equal to zero."));
        if(bindingResult.hasErrors())
            return returnBindingResult(model, user, swap);
        if(swap.getFrom().equals(swap.getTo()))
            bindingResult.addError(new FieldError("swap", "to", "You cannot swap to the same currency you are swapping from."));

        if(swap.getFrom().equals("US Dollar")) {
            if(swap.getQuantity() < 1) {
                bindingResult.addError(new FieldError(
                        "swap", "quantity", "Minimum swap price must be at least 1 USD."
                ));
                return returnBindingResult(model, user, swap);
            }
            CryptoNamePrice price = service.getCryptoNameBySymbol(swap.getTo());
            toPrice = price.getPrice();
            toName = price.getName();
            float quantityPriceTo = (float) (swap.getQuantity() / price.getPrice());
            float fee = quantityPriceTo * feePercentage;
            quantityPriceTo -= fee;
        } else {
            List<CryptoNamePrice> prices = service.getPricesBySymbols(swap.getFrom(), swap.getTo());
            if(prices.size() < 2) {
                bindingResult.addError(new FieldError(
                        "swap", "to", "One or more of the currencies you selected are not valid."
                ));
            }
            quantityPriceFrom = (float) (prices.getFirst().getPrice() * swap.getQuantity());
            if(quantityPriceFrom < 1)
                bindingResult.addError(new FieldError(
                        "swap", "quantity", "Minimum swap price must be at least 1 USD."
                ));
            toPrice = prices.getLast().getPrice();
            toName = prices.getLast().getName();
        }
        if(bindingResult.hasErrors())
            return returnBindingResult(model, user, swap);

        float quantityPriceTo = (float) (quantityPriceFrom / toPrice);
        float fee = quantityPriceTo * feePercentage;
        quantityPriceTo -= fee;

        appendToRepository(swap, user.getId(), toName, quantityPriceTo);

        populateModel(model, user);
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
