package com.market.tradingbit.mappers;

import com.market.tradingbit.dtos.HistoryDto;
import com.market.tradingbit.dtos.SuccessfulSwapDto;
import com.market.tradingbit.entities.History;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoryMapper {
    History toHistory(SuccessfulSwapDto successfulSwapDto);
    History toHistory(HistoryDto historyDto);
}
