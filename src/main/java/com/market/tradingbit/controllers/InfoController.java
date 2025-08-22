package com.market.tradingbit.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class InfoController {

    @GetMapping("/docs")
    public String docs() {
        return "document";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }
}
