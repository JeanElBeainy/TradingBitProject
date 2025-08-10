package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.LoginDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class AuthController {

    @GetMapping
    public String login(Model model) {
        model.addAttribute("loginDto", new LoginDto());
        return "signin";
    }
}
