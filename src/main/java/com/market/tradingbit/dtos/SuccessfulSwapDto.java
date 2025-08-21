package com.market.tradingbit.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessfulSwapDto {
    private String fromSymbol;
    private String toSymbol;
    private String fromQuantity;
    private String toQuantity;
    private String fee;
}
