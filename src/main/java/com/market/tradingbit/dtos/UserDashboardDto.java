package com.market.tradingbit.dtos;

import lombok.Data;

@Data
public class UserDashboardDto {
    private String name;
    private String usdBalance;
    private String cryptoBalance;
    private String stockBalance;
    private String totalBalance;
}
