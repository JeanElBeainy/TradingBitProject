package com.market.tradingbit.controllers;

import com.market.tradingbit.entities.User;
import com.market.tradingbit.helpers.UpdateUserDetail;
import com.market.tradingbit.models.CryptoInfo;
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
    private final CoinMarketCapService service;
    private final UpdateUserDetail updateUserDetail;

    @GetMapping
    public String dashboard(Model model, Principal principal) {
        User user = null;
        if(principal != null)
            user = userRepository.findByEmail(principal.getName());
        if(user != null)
            updateUserDetail.setUpUserDashboard(model, user);

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

    @GetMapping("/update-user-info")
    public String updateUserInfo(Model model, Principal principal) {
        if(principal != null)
            updateUserDetail.setUpUserDashboard(model, userRepository.findByEmail(principal.getName()));
        return "dashboard :: update-user-info";
    }
}
