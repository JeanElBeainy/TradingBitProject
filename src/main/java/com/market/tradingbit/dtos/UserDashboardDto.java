package com.market.tradingbit.dtos;

import com.market.tradingbit.entities.Role;
import lombok.Data;

@Data
public class UserDashboardDto {
    private String name;
    private Role role;
    private String usdBalance;
    private String cryptoBalance;
    private String stockBalance;
    private String totalBalance;
    private String totalVolume;
}
