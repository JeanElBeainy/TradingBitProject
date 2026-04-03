package com.market.tradingbit.models;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class CmcResponse {
    private List<CryptoInfo> data;
}
