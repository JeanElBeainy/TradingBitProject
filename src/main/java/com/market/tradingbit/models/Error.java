package com.market.tradingbit.models;

import com.market.tradingbit.dtos.SwapDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Getter
@AllArgsConstructor
public class Error {
    private Model model;
    private Long userId;
    private SwapDto swap;
    private String message;
    private BindingResult bindingResult;
}
