package com.market.tradingbit.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Data
@AllArgsConstructor
public class UserModelBinding {
    Model model;
    Long userId;
    BindingResult bindingResult;
}
