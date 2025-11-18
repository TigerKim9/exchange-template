package com.exchange.crypto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trading_pairs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_currency", nullable = false)
    private String baseCurrency; // BTC, ETH, etc.

    @Column(name = "quote_currency", nullable = false)
    private String quoteCurrency; // USD, USDT, etc.

    @Column(name = "symbol", unique = true, nullable = false)
    private String symbol; // BTC/USD, ETH/USDT, etc.

    @Column(name = "current_price", precision = 20, scale = 8)
    private BigDecimal currentPrice;

    @Column(name = "high_24h", precision = 20, scale = 8)
    private BigDecimal high24h;

    @Column(name = "low_24h", precision = 20, scale = 8)
    private BigDecimal low24h;

    @Column(name = "volume_24h", precision = 20, scale = 8)
    private BigDecimal volume24h;

    @Column(name = "price_change_24h", precision = 20, scale = 8)
    private BigDecimal priceChange24h;

    @Column(name = "price_change_percent_24h", precision = 10, scale = 2)
    private BigDecimal priceChangePercent24h;

    @Column(name = "min_order_amount", precision = 20, scale = 8)
    private BigDecimal minOrderAmount;

    @Column(name = "max_order_amount", precision = 20, scale = 8)
    private BigDecimal maxOrderAmount;

    @Column(name = "trading_fee", precision = 10, scale = 4)
    private BigDecimal tradingFee = BigDecimal.valueOf(0.001); // 0.1% default

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
