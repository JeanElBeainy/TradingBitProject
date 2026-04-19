package com.market.tradingbit.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_symbol")
    private String fromSymbol;

    @Column(name = "from_name")
    private String fromName;

    @Column(name = "from_quantity", precision = 30, scale = 8)
    private BigDecimal fromQuantity;

    @Column(name = "from_price", precision = 30, scale = 8)
    private BigDecimal fromPrice;

    @Column(name = "to_symbol")
    private String toSymbol;

    @Column(name = "to_name")
    private String toName;

    @Column(name = "to_quantity", precision = 30, scale = 8)
    private BigDecimal toQuantity;

    @Column(name = "to_price", precision = 30, scale = 8)
    private BigDecimal toPrice;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "volume", precision = 30, scale = 8)
    private BigDecimal volume;

    @Column(name = "fee", precision = 30, scale = 8)
    private BigDecimal fee;

    @Column(name = "time")
    private String time;

    @Column(name = "purchase_type")
    @Enumerated(EnumType.STRING)
    private Type type;

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", fromSymbol='" + fromSymbol + '\'' +
                ", fromName='" + fromName + '\'' +
                ", fromQuantity=" + fromQuantity +
                ", fromPrice=" + fromPrice +
                ", toSymbol='" + toSymbol + '\'' +
                ", toName='" + toName + '\'' +
                ", toQuantity=" + toQuantity +
                ", toPrice=" + toPrice +
                ", userId=" + userId +
                ", volume=" + volume +
                ", fee=" + fee +
                '}';
    }

}
