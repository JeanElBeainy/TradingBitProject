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
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.Port;
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
        model.addAttribute("swap", new SwapDto());
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
        return "swap";
    }

    @PostMapping("/crypto")
    public String cryptoSwap(Model model, @Valid @ModelAttribute("swap") SwapDto swap, Principal principal, BindingResult bindingResult) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        if(swap.getFrom() == null || swap.getFrom().isEmpty()) {
            bindingResult.addError(new FieldError(
                    "swap", "from", "Please select a valid currency that you own."
            ));
        }
        if(swap.getTo() == null || swap.getTo().isEmpty()) {
            bindingResult.addError(new FieldError(
                    "swap", "to", "Please select a valid currency to swap."
            ));
        }
        if(swap.getQuantity() <= 0) {
            bindingResult.addError(new FieldError(
                    "swap", "quantity", "Quantity cannot be less than or equal to zero."
            ));
        }
        List<CryptoNamePrice> prices = service.getPricesBySymbols(swap.getFrom(), swap.getTo());
        if(prices.size() < 2) {
            bindingResult.addError(new FieldError(
                    "swap", "quantity", "One or more of the currencies you selected are not valid."
            ));
        }
        if(bindingResult.hasErrors()) {
            populateModel(model, user);
            return "swap";
        }
        System.out.println(prices.getFirst() + " " + prices.getLast());
        float quantityPriceFrom = (float) (prices.getFirst().getPrice() * swap.getQuantity());
        System.out.println("quantity price from: " + quantityPriceFrom);
        float quantityPriceTo = (float) (quantityPriceFrom / prices.getLast().getPrice());
        System.out.println("quantity price to: " + quantityPriceTo);
        if(portfolioRepository.getItemBySymbolAndUserId(swap.getTo(), user.getId()) == null) {
            portfolioRepository.save(Portfolio.builder()
                            .symbol(swap.getTo())
                            .purchaseType(Type.CRYPTO)
                            .userId(user.getId())
                            .name(prices.getLast().getName())
                    .build());
        }
        portfolioRepository.updatePortfolioQuantity(swap.getQuantity()*-1, swap.getFrom(), user.getId());
        portfolioRepository.updatePortfolioQuantity(quantityPriceTo, swap.getTo(), user.getId());
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
