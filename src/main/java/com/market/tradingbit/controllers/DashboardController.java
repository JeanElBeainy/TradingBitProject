package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.UserDashboardDto;
import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sound.sampled.Port;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
@AllArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final CoinMarketCapService service;

    @GetMapping
    public String dashboard(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        UserDashboardDto userDashboardDto = new UserDashboardDto();
        userDashboardDto.setName(user.getName());

        System.out.println(user.getId());

        Portfolio portfolio = portfolioRepository.findById(user.getId()).orElseThrow();
        System.out.println(portfolio);
        userDashboardDto.setUsdBalance(portfolio.getUsdBalance());
        userDashboardDto.setCryptoBalance(portfolio.getCryptoBalance());
        userDashboardDto.setStockBalance(portfolio.getStockBalance());
        userDashboardDto.setTotalBalance(portfolio.getTotalBalance());

        model.addAttribute("userDashboardDto", userDashboardDto);
        model.addAttribute("cryptos", service.getLatestListings());
        return "dashboard";
    }
}
