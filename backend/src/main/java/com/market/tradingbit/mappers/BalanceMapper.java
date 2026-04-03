package com.market.tradingbit.mappers;

import com.market.tradingbit.dtos.UserDashboardDto;
import com.market.tradingbit.entities.Balance;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BalanceMapper {
    UserDashboardDto toUserDashboardDto(Balance balance);
}
