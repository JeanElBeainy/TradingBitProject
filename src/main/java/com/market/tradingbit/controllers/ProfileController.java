package com.market.tradingbit.controllers;

import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.helpers.UpdateUserDetail;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/profile")
@AllArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;;
    private final UpdateUserDetail updateUserDetail;

    private void getUserInfo(Model model, User user) {
        updateUserDetail.setUpUserDashboard(model, user);
    }

    @GetMapping
    public String profile(Principal principal, Model model) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        getUserInfo(model, user);

        List<History> history = historyRepository.findAllByIdDesc(user.getId());
        model.addAttribute("history", history);

        return "profile";
    }
}
