package com.market.tradingbit.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserDashboardDto {
    private String name;
    private String usdBalance;
    private String cryptoBalance;
    private String stockBalance;
    private String totalBalance;
}
