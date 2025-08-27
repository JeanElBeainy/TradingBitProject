package com.market.tradingbit.controllers;

import com.market.tradingbit.helpers.UserDashboard;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;

@Controller
@RequestMapping("/dashboard")
@AllArgsConstructor
public class DashboardController {

    private UserDashboard userDashboard;

    @GetMapping
    public String dashboard(Model model, Principal principal) {
        userDashboard.getDashboard(model, principal);
        return "dashboard";
    }

    @GetMapping("/update-table")
    public String getCryptoTableFragment(Model model) {
        userDashboard.getCryptoTable(model);
        return "dashboard :: crypto-table-body";
    }

    @GetMapping("/update-user-info")
    public String updateUserInfo(Model model, Principal principal) {
        userDashboard.updateUserInfo(model, principal);
        return "dashboard :: update-user-info";
    }
}
