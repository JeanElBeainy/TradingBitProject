package com.market.tradingbit.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "asset")
public class Asset {

    @Id
    @Column(name = "symbol")
    private String symbol;

    @Column(name = "name", nullable = false)
    private String name;
}
