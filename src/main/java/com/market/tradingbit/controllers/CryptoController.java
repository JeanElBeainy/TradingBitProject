package com.market.tradingbit.controllers;

import com.market.tradingbit.dtos.ChartData;
import com.market.tradingbit.dtos.PricePoint;
import com.market.tradingbit.services.CoinGeckoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/crypto")
public class CryptoController {

    private final CoinGeckoService service;

    @GetMapping("{symbol}")
    public String crypto(@PathVariable("symbol") String symbol, @RequestParam(defaultValue = "30") int days, Model model) {
        ChartData data = service.getCryptoChartData(symbol, days);
        List<PricePoint> pricePoints = data.getPrices().stream()
                .map(list -> {
                    PricePoint pp = new PricePoint();
                    pp.setDate(new Date(list.get(0).longValue())); // timestamp → Date
                    pp.setPrice(list.get(1));
                    return pp;
                })
                .toList();
        model.addAttribute("chart", pricePoints);
        model.addAttribute("symbol", symbol);
        model.addAttribute("days", days);
        return "cryptoPage";
    }
}
