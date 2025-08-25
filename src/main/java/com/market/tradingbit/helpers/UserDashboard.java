package com.market.tradingbit.helpers;

import com.market.tradingbit.entities.User;
import com.market.tradingbit.models.CryptoInfo;
import com.market.tradingbit.repositories.UserRepository;
import com.market.tradingbit.services.CoinMarketCapService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@AllArgsConstructor
public class UserDashboard {

    private final UserRepository userRepository;
    private final CoinMarketCapService service;
    private final UpdateUserDetail updateUserDetail;

    public void getDashboard(Model model, Principal principal) {
        User user = null;
        if(principal != null)
            user = userRepository.findByEmail(principal.getName());
        if(user != null)
            updateUserDetail.setUpUserDashboard(model, user);

        model.addAttribute("cryptos", service.getLatestListings());
        model.addAttribute("lastUpdated", new SimpleDateFormat("MMM dd, HH:mm:ss").format(new Date()));
    }

    public void getCryptoTable(Model model) {
        List<CryptoInfo> cryptos = service.getLatestListings();
        model.addAttribute("cryptos", cryptos);
    }

    public void updateUserInfo(Model model, Principal principal) {
        if(principal != null)
            updateUserDetail.setUpUserDashboard(model, userRepository.findByEmail(principal.getName()));
    }
}
