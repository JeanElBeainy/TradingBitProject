package com.market.tradingbit.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_symbol")
    private String fromSymbol;

    @Column(name = "from_name")
    private String fromName;

    @Column(name = "from_quantity")
    private float fromQuantity;

    @Column(name = "from_price")
    private double fromPrice;

    @Column(name = "to_symbol")
    private String toSymbol;

    @Column(name = "to_name")
    private String toName;

    @Column(name = "to_quantity")
    private float toQuantity;

    @Column(name = "to_price")
    private double toPrice;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "volume")
    private float volume;

    @Column(name = "fee")
    private float fee;
}
