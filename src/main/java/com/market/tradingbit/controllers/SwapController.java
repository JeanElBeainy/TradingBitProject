package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.*;
import com.market.tradingbit.helpers.*;
import com.market.tradingbit.models.*;
import com.market.tradingbit.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@AllArgsConstructor
@RequestMapping("/swap")
public class SwapController {

    private final UserRepository userRepository;
    private final SwapValidation swapValidation;
    private final UserSwap userSwap;

    @GetMapping("/crypto")
    public String cryptoSwap(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        userSwap.getCryptoSwap(model, principal);
        return "swap";
    }

    @GetMapping("/update-prices")
    public String updatePrices(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        swapValidation.populateModelToUser(model, user.getId());
        return "swap :: update-prices";
    }

    @PostMapping("/crypto")
    public String cryptoSwap(Model model, @Valid @ModelAttribute("swap") SwapDto swap, Principal principal, BindingResult bindingResult) {
        if(principal == null) return "redirect:/login";
        User user = userRepository.findByEmail(principal.getName());
        return userSwap.userSwap(model, swap, user.getId(), bindingResult);
    }
}