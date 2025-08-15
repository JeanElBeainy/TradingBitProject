package com.market.tradingbit.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "portfolio")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "purchase_type")
    @Enumerated(EnumType.STRING)
    private Type purchaseType;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "quantity")
    private float quantity;

}
