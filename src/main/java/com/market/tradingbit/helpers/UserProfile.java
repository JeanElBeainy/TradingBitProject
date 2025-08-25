package com.market.tradingbit.helpers;

import com.market.tradingbit.entities.History;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@AllArgsConstructor
public class UserProfile {

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;;
    private final UpdateUserDetail updateUserDetail;

    public String setupProfile(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        updateUserDetail.setUpUserDashboard(model, user);

        List<History> history = historyRepository.findAllByIdDesc(user.getId());
        model.addAttribute("history", history);
        model.addAttribute("lastUpdated", new SimpleDateFormat("MMM dd, HH:mm:ss").format(new Date()));
        return "profile";
    }

    public void updateUserInfo(Model model, Principal principal) {
        if(principal != null)
            updateUserDetail.setUpUserDashboard(model, userRepository.findByEmail(principal.getName()));
    }
}
