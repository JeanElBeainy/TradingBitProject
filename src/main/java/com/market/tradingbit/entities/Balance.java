package com.market.tradingbit.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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
    private double cryptoBalance;

    @Column(name = "stock_balance")
    private double stockBalance;

    @Column(name = "usd_balance")
    private double usdBalance;

    @Column(name = "total_balance")
    private double totalBalance;

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
