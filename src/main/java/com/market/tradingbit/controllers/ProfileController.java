package com.market.tradingbit.controllers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@AllArgsConstructor
public class ProfileController {

    @RequestMapping
    public String profile() {
        return "profile";
    }
}
