package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.UserDashboardDto;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.repositories.UserRepository;
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

    private final UserRepository userRepository;

    @GetMapping
    public String dashboard(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        UserDashboardDto userDashboardDto = new UserDashboardDto();
        userDashboardDto.setName(user.getName());
        model.addAttribute("userDashboardDto", userDashboardDto);
        return "dashboard";
    }
}
