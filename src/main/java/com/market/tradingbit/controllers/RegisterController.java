package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.RegisterDto;
import com.market.tradingbit.entities.*;
import com.market.tradingbit.helpers.registration.RegisterUser;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/signup")
public class RegisterController {

    private final RegisterUser register;

    @GetMapping
    public String register(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        model.addAttribute("success", false);
        return "signup";
    }

    @PostMapping
    public String register(Model model, @Valid @ModelAttribute RegisterDto registerDto, BindingResult bindingResult) {
        return register.registerUser(model, registerDto, bindingResult);
    }
}
