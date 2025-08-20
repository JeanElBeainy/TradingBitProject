package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.UserDashboardDto;
import com.market.tradingbit.entities.Balance;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.mappers.BalanceMapper;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;

@Controller
@RequestMapping("/profile")
@AllArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final PortfolioRepository portfolioRepository;
    private final HistoryRepository historyRepository;
    private final BalanceMapper balanceMapper;
    private final CoinMarketCapService service;

    private void getUserInfo(Model model, User user) {
        DashboardController.GetUserDashboard(model,
                user,
                service,
                portfolioRepository,
                balanceRepository,
                balanceMapper
        );
    }

    @GetMapping
    public String profile(Principal principal, Model model) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        getUserInfo(model, user);
        return "profile";
    }
}
