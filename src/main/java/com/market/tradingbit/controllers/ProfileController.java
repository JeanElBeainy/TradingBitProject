package com.market.tradingbit.controllers;

import com.market.tradingbit.helpers.UserProfile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/profile")
@AllArgsConstructor
public class ProfileController {

    private UserProfile userProfile;

    @GetMapping
    public String profile(Principal principal, Model model) {
        return userProfile.setupProfile(model, principal);
    }

    @GetMapping("/update-balance")
    public String updateUserInfo(Model model, Principal principal) {
        userProfile.updateUserInfo(model, principal);
        return "profile :: update-balance";
    }

    @GetMapping("/last-updated")
    public String getLastUpdatedTime(Model model) {
        model.addAttribute("lastUpdated", System.currentTimeMillis());
        return "profile :: last-updated";
    }
}
