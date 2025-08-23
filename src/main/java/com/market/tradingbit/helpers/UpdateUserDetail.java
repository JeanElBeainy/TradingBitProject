package com.market.tradingbit.helpers;

import com.market.tradingbit.dtos.UserDashboardDto;
import com.market.tradingbit.entities.Balance;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.mappers.BalanceMapper;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import java.math.BigDecimal;

@Component
@AllArgsConstructor
public class UpdateUserDetail {

    private final BalanceRepository balanceRepository;
    private final CoinMarketCapService service;
    private final PortfolioRepository portfolioRepository;
    private final BalanceMapper balanceMapper;

    private void updateUserBalance(Long userId) {
        BigDecimal cryptoBalance = service.calculatePortfolioValue(
                portfolioRepository.getCryptoSymbolAndQuantityByUserId(userId)
        );
        balanceRepository.updateCryptoBalanceByAmountAndUserId(cryptoBalance, userId);
    }

    private void GetUserDashboard(User user, Model model) {
        updateUserBalance(user.getId());
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

    public void setUpUserDashboard(Model model, User user) {
        GetUserDashboard(user, model);
    }

    public void updateBalance(Long userId) {
        updateUserBalance(userId);
    }
}
