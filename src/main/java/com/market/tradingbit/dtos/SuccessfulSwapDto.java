package com.market.tradingbit.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessfulSwapDto {
    private String from;
    private String to;
    private float fromQuantity;
    private float toQuantity;
    private float fee;
}
