package com.market.tradingbit.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ChartData {
    private List<List<Double>> prices;
    private List<List<Double>> market_caps;
    private List<List<Double>> total_volumes;
}
