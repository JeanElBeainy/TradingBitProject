package com.market.tradingbit.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessfulSwapDto {
    private String from;
    private String to;
    private String fromQuantity;
    private String toQuantity;
    private String fee;
}
