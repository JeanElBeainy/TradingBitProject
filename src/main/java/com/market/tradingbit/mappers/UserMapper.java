package com.market.tradingbit.mappers;

import com.market.tradingbit.dtos.RegisterDto;
import com.market.tradingbit.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDto(RegisterDto registerDto);
}
