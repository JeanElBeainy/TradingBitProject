package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.UserDashboardDto;
import com.market.tradingbit.entities.Balance;
import com.market.tradingbit.entities.User;
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

    @GetMapping
    public String dashboard(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        UserDashboardDto userDashboardDto = new UserDashboardDto();
        userDashboardDto.setName(user.getName());

        System.out.println(user.getId());

        Balance balance = balanceRepository.findById(user.getId()).orElseThrow();
        balanceRepository.updateUSDBalanceByAmountAndUserId(
                portfolioRepository.getQuantityBySymbolAndUserId("US Dollar", user.getId()),
                user.getId());
        System.out.println(balance);
        userDashboardDto.setUsdBalance(balance.getUsdBalance());
        userDashboardDto.setCryptoBalance(balance.getCryptoBalance());
        userDashboardDto.setStockBalance(balance.getStockBalance());
        userDashboardDto.setTotalBalance(balance.getTotalBalance());

        model.addAttribute("userDashboardDto", userDashboardDto);
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
