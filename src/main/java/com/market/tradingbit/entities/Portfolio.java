package com.market.tradingbit.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "portfolio")
public class Portfolio {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "purchase_type")
    @Enumerated(EnumType.STRING)
    private Type purchaseType;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "quantity")
    private float quantity;

}
