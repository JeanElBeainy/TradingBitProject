package com.market.tradingbit.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserDashboardDto {
    private String name;
    private BigDecimal usdBalance;
    private BigDecimal cryptoBalance;
    private BigDecimal stockBalance;
    private BigDecimal totalBalance;
}
