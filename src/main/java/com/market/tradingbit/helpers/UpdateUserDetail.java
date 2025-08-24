package com.market.tradingbit.helpers;

import com.market.tradingbit.dtos.UserDashboardDto;
import com.market.tradingbit.entities.Balance;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.mappers.BalanceMapper;
import com.market.tradingbit.repositories.BalanceRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import java.math.BigDecimal;
import java.util.Objects;

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

        userDashboard.setTotalVolume(balance.getTotalVolume().toString());
        userDashboard.setCryptoBalance(formatBalance(balance.getCryptoBalance()));
        userDashboard.setStockBalance(formatBalance(balance.getStockBalance()));
        userDashboard.setTotalVolume(formatBalance(balance.getTotalVolume()));
        model.addAttribute("userDashboardDto", userDashboard);
    }

    private String formatBalance(BigDecimal value) {
        if (Objects.equals(value.toString(), "0E-8"))
            return "0.0";
        return value.toPlainString();
    }

    public void setUpUserDashboard(Model model, User user) {
        GetUserDashboard(user, model);
    }
}
