package com.market.tradingbit.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PricePoint {
    private Date date;
    private double price;
}
