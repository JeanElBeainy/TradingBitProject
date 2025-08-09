package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.RegisterDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/signup")
public class RegisterController {

    @GetMapping
    public String register(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "signup";
    }
}
