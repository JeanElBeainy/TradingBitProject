package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.ChartData;
import com.market.tradingbit.dtos.PricePoint;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import com.market.tradingbit.services.CoinGeckoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Date;
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

    @GetMapping("{symbol}")
    public String crypto(@PathVariable("symbol") String symbol, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        List<Portfolio> portfolioList = portfolioRepository.getPortfolioById(user.getId());
        System.out.println(portfolioList.getFirst().getSymbol());
        System.out.println(portfolioList.getFirst().getQuantity());
        System.out.println(portfolioList.getFirst().getId());
        System.out.println(portfolioList.getFirst().getPurchaseType());
        return "swap";
    }
}
