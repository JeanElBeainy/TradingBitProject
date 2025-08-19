package com.market.tradingbit.models;

import com.market.tradingbit.dtos.SwapDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BasicUserError {
    Model model;
    BigDecimal swapQuantity;
    SwapDto swap;
    Long userId;
    BindingResult bindingResult;
}
