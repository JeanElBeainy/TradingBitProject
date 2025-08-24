package com.market.tradingbit.mappers;

import com.market.tradingbit.dtos.PortfolioDto;
import com.market.tradingbit.entities.Portfolio;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {
    List<PortfolioDto> toPortfolioDto(List<Portfolio> portfolio);
}
