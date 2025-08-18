package com.market.tradingbit.mappers;

import com.market.tradingbit.dtos.SuccessfulSwapDto;
import com.market.tradingbit.entities.History;
import com.market.tradingbit.models.FromUSD;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoryMapper {
    History toHistory(SuccessfulSwapDto successfulSwapDto);
    History toHistory(FromUSD fromUSD);
}
