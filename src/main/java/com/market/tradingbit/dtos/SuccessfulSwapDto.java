package com.market.tradingbit.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SuccessfulSwapDto {
    private String from;
    private String to;
    private String fromQuantity;
    private String toQuantity;
    private String fee;
}
