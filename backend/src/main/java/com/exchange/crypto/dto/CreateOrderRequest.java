package com.exchange.crypto.dto;

import com.exchange.crypto.model.Order;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    @NotNull
    private Long tradingPairId;

    @NotNull
    private Order.OrderType type;

    @NotNull
    private Order.OrderSide side;

    @NotNull
    @DecimalMin(value = "0.00000001")
    private BigDecimal price;

    @NotNull
    @DecimalMin(value = "0.00000001")
    private BigDecimal amount;

    private BigDecimal stopPrice;
}
