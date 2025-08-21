package com.market.tradingbit.helpers;

import com.market.tradingbit.dtos.SwapDto;
import com.market.tradingbit.entities.SwapErrorType;
import com.market.tradingbit.models.CryptoNamePrice;
import com.market.tradingbit.models.Error;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.market.tradingbit.helpers.SwapValidation.parseQuantity;

@Component
@AllArgsConstructor
public class CurrencyValidation {

    private final ApiService apiService;
    private final SwapValidation swapValidation;

//    public String x(SwapDto swap, Error error) {
//        CryptoNamePrice price = apiService.getCryptoPrice(swap.getTo());
//        BigDecimal swapQuantity = parseQuantity(swap.getQuantity());
//        if((swapQuantity.multiply(BigDecimal.valueOf(price.getPrice()))).compareTo(MINIMUM_SWAP_USD) < 0)
//            return swapValidation.swapError(error, SwapErrorType.QUANTITY);
//    }
}
