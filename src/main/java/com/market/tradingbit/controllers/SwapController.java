package com.market.tradingbit.controllers;

import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.models.CryptoNameSymbol;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/swap")
public class SwapController {

    private final CoinMarketCapService service;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @GetMapping("/crypto")
    public String cryptoSwap(Model model, Principal principal) {
        User user;
        try {
            user = userRepository.findByEmail(principal.getName());
        } catch (Exception e) {
            System.out.println("Exception with swap: " + e.getMessage());
            return "swap";
        }
        List<Portfolio> portfolioList = portfolioRepository.getCryptoPortfolioByUserId(user.getId());
        model.addAttribute("userItems", portfolioList);

        List<CryptoNameSymbol> latestListings = service.getLatestNameAndSymbol();
        model.addAttribute("swapItems", latestListings);
        return "swap";
    }

    @GetMapping("/stock")
    public String stockSwap(Model model, Principal principal) {
        List<Portfolio> portfolioList = portfolioRepository.getStockPortfolioByUserId(userRepository.findByEmail(principal.getName()).getId());
        model.addAttribute("userItems", portfolioList);
        return "swap";
    }
}
