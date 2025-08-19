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

    @GetMapping
    public String dashboard(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        UserDashboardDto userDashboardDto = new UserDashboardDto();
        userDashboardDto.setName(user.getName());

        //TODO: if value = BigDecimal.ZERO, set 0.0 as value.
        balanceRepository.updateUSDBalanceByAmountAndUserId(
                portfolioRepository.getQuantityBySymbolAndUserId("US Dollar", user.getId()),
                user.getId());

        BigDecimal cryptoBalance = service.calculatePortfolioValue(
                portfolioRepository.getCryptoSymbolAndQuantityByUserId(user.getId())
        );
        balanceRepository.updateCryptoBalanceByAmountAndUserId(cryptoBalance, user.getId());

        Balance balance = balanceRepository.findById(user.getId()).orElseThrow();
        balance.setTotalBalance(balance.getUsdBalance().add(balance.getCryptoBalance()).add(balance.getStockBalance()));

        model.addAttribute("userDashboardDto", balanceMapper.toUserDashboardDto(balance));
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
