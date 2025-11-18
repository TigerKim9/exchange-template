package com.exchange.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketUpdate {
    private String symbol;
    private BigDecimal price;
    private BigDecimal volume;
    private BigDecimal high24h;
    private BigDecimal low24h;
    private BigDecimal priceChange24h;
    private BigDecimal priceChangePercent24h;
    private LocalDateTime timestamp;
}
