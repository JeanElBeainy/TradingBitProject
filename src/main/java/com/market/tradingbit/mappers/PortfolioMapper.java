package com.market.tradingbit.mappers;

import com.market.tradingbit.dtos.PortfolioDto;
import com.market.tradingbit.entities.Portfolio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {
    @Mapping(source = "asset.name", target = "name")
    @Mapping(target = "price", ignore = true)
    PortfolioDto toPortfolioDto(Portfolio portfolio);

    List<PortfolioDto> toPortfolioDto(List<Portfolio> portfolio);
}
