package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.*;
import com.market.tradingbit.helpers.*;
import com.market.tradingbit.models.*;
import com.market.tradingbit.repositories.HistoryRepository;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/swap")
public class SwapController {

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final SwapValidation swapValidation;
    private final UserSwap userSwap;

    @GetMapping("/crypto")
    public String cryptoSwap(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        swapValidation.populateModelToUser(model, user.getId());
        model.addAttribute("swap", new SwapDto());
        model.addAttribute("success", false);
        List<History> history = historyRepository.findTop3ByUserIdOrderByIdDesc(user.getId());
        model.addAttribute("history", history);
        model.addAttribute("lastUpdated", new SimpleDateFormat("MMM dd, HH:mm:ss").format(new Date()));
        return "swap";
    }

    @GetMapping("/update-all")
    public String updatePrices(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        swapValidation.populateModelToUser(model, user.getId());
        return "swap :: update-prices";
    }

    @GetMapping("/last-updated")
    public String getLastUpdatedTime(Model model) {
        model.addAttribute("lastUpdated", new SimpleDateFormat("MMM dd, HH:mm:ss").format(new Date()));
        return "swap :: last-updated";
    }

    @PostMapping("/crypto")
    public String cryptoSwap(Model model, @Valid @ModelAttribute("swap") SwapDto swap, Principal principal, BindingResult bindingResult) {
        if(principal == null) return "redirect:/login";
        System.out.println("Post Mapping:");
        User user = userRepository.findByEmail(principal.getName());
        return userSwap.userSwap(model, swap, user.getId(), bindingResult);
    }
}