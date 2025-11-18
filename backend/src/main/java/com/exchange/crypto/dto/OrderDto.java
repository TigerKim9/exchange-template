package com.exchange.crypto.dto;

import com.exchange.crypto.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String tradingPairSymbol;
    private Order.OrderType type;
    private Order.OrderSide side;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal filledAmount;
    private BigDecimal remainingAmount;
    private BigDecimal total;
    private BigDecimal fee;
    private Order.OrderStatus status;
    private LocalDateTime createdAt;
}
