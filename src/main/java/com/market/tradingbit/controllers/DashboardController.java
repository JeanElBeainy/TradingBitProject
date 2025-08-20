package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.UserDashboardDto;
import com.market.tradingbit.entities.Balance;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.mappers.BalanceMapper;
import com.market.tradingbit.models.CryptoInfo;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.math.BigDecimal;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
@AllArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final CoinMarketCapService service;
    private final PortfolioRepository portfolioRepository;
    private final BalanceMapper balanceMapper;

    private void setUpUserDashboard(Model model, User user) {
        GetUserDashboard(model, user, service, portfolioRepository, balanceRepository, balanceMapper);
    }

    public static void GetUserDashboard(Model model, User user, CoinMarketCapService service, PortfolioRepository portfolioRepository, BalanceRepository balanceRepository, BalanceMapper balanceMapper) {
        BigDecimal cryptoBalance = service.calculatePortfolioValue(
                portfolioRepository.getCryptoSymbolAndQuantityByUserId(user.getId())
        );
        balanceRepository.updateCryptoBalanceByAmountAndUserId(cryptoBalance, user.getId());

        Balance balance = balanceRepository.findById(user.getId()).orElseThrow();
        UserDashboardDto userDashboard = balanceMapper.toUserDashboardDto(balance);
        userDashboard.setName(user.getName());
        userDashboard.setRole(user.getRole());

        //TODO: check why it's not being mapped in BalanceMapper
        userDashboard.setTotalVolume(balance.getTotalVolume().toString());

        if(userDashboard.getCryptoBalance().equals("0E-8"))
            userDashboard.setCryptoBalance("0.0");
        if(userDashboard.getStockBalance().equals("0E-8"))
            userDashboard.setStockBalance("0.0");
        if(userDashboard.getTotalVolume().equals("0E-8"))
            userDashboard.setTotalVolume("0.0");
        model.addAttribute("userDashboardDto", userDashboard);
    }

    @GetMapping
    public String dashboard(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        setUpUserDashboard(model, user);

        model.addAttribute("cryptos", service.getLatestListings());
        model.addAttribute("lastUpdated", new SimpleDateFormat("MMM dd, HH:mm:ss").format(new Date()));
        return "dashboard";
    }

    @GetMapping("/update-table")
    public String getCryptoTableFragment(Model model) {
        List<CryptoInfo> cryptos = service.getLatestListings();
        model.addAttribute("cryptos", cryptos);
        return "dashboard :: crypto-table-body";
    }

    @GetMapping("/last-updated")
    public String getLastUpdatedTime(Model model) {
        model.addAttribute("lastUpdated", new SimpleDateFormat("MMM dd, HH:mm:ss").format(new Date()));
        return "dashboard :: last-updated";
    }
}
