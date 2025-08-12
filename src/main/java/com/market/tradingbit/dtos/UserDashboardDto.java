package com.market.tradingbit.dtos;

import lombok.Data;

@Data
public class UserDashboardDto {
    private String name;
    private double usdBalance;
    private double cryptoBalance;
    private double stockBalance;
    private double totalBalance;
}
