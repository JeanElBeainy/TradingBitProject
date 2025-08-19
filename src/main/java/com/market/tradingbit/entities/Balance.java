package com.market.tradingbit.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "balance")
public class Balance {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "crypto_balance")
    private BigDecimal cryptoBalance;

    @Column(name = "stock_balance")
    private BigDecimal stockBalance;

    @Column(name = "usd_balance")
    private BigDecimal usdBalance;

    @Column(name = "total_balance")
    private BigDecimal totalBalance;

    @Override
    public String toString() {
        return "Portfolio{" +
                "id=" + id +
                ", cryptoBalance=" + cryptoBalance +
                ", stockBalance=" + stockBalance +
                ", usdBalance=" + usdBalance +
                ", totalBalance=" + totalBalance +
                '}';
    }
}
