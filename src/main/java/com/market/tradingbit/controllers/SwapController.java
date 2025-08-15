package com.market.tradingbit.controllers;

import com.market.tradingbit.entities.Portfolio;
import com.market.tradingbit.entities.Type;
import com.market.tradingbit.entities.User;
import com.market.tradingbit.repositories.PortfolioRepository;
import com.market.tradingbit.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/swap")
public class SwapController {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @GetMapping("/crypto")
    public String cryptoSwap(Model model, Principal principal) {
        User user;
        try {
            user = userRepository.findByEmail(principal.getName());
//            Portfolio portfolio = Portfolio.builder()
//                    .symbol("ETH")
//                    .name("Ethereum")
//                    .purchaseType(Type.CRYPTO)
//                    .quantity(2.0032F)
//                    .userId(user.getId())
//                    .build();
//            portfolioRepository.save(portfolio);
        } catch (Exception e) {
            System.out.println("Exception with swap: " + e.getMessage());
            return "swap";
        }
        List<Portfolio> portfolioList = portfolioRepository.getPortfolioByUserId(user.getId());
        model.addAttribute("userItems", portfolioList);
        return "swap";
    }

    @GetMapping("/stock")
    public String stockSwap(Model model, Principal principal) {

        return "swap";
    }
}
