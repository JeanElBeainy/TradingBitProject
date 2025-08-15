package com.market.tradingbit.controllers;

import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/swap")
public class SwapController {

    //private final CoinGeckoService service;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String swap(Principal principal) {
        if(principal == null) return "redirect:/login";
        return "swap";
    }

    //ISSUE: Function looping 3 times
    @GetMapping("/{symbol}")
    public String swap(@PathVariable("symbol") String symbol, Model model, Principal principal) {
        User user;
        try {
            user = userRepository.findByEmail(principal.getName());
            Portfolio portfolio = Portfolio.builder()
                    .symbol("BTC")
                    .name("Bitcoin")
                    .purchaseType(Type.CRYPTO)
                    .quantity(1)
                    .userId(user.getId())
                    .build();
            System.out.println(portfolio);
            portfolioRepository.save(portfolio);
        } catch (Exception e) {
            System.out.println("Exception with swap: " + e.getMessage());
            return "swap";
        }
        System.out.println(portfolioRepository.getPortfolioSymbolsByUserId(user.getId()));
        List<Portfolio> portfolioList = portfolioRepository.getPortfolioByUserId(user.getId());
        System.out.println(portfolioList.size());
        model.addAttribute("userItems", portfolioList);
        portfolioRepository.updatePortfolioQuantity(0.5F, "BTC", user.getId());
        return "swap";
    }
}
