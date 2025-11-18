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
@Table(name = "orders", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_trading_pair_id", columnList = "trading_pair_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trading_pair_id", nullable = false)
    private TradingPair tradingPair;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type; // MARKET, LIMIT, STOP_LOSS, STOP_LIMIT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side; // BUY, SELL

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "filled_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal filledAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal remainingAmount;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal total; // price * amount

    @Column(precision = 20, scale = 8)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "stop_price", precision = 20, scale = 8)
    private BigDecimal stopPrice; // For stop orders

    @Column(name = "time_in_force")
    @Enumerated(EnumType.STRING)
    private TimeInForce timeInForce = TimeInForce.GTC; // Good Till Cancelled

    @Column(name = "filled_at")
    private LocalDateTime filledAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderType {
        MARKET, LIMIT, STOP_LOSS, STOP_LIMIT
    }

    public enum OrderSide {
        BUY, SELL
    }

    public enum OrderStatus {
        PENDING, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED
    }

    public enum TimeInForce {
        GTC, // Good Till Cancelled
        IOC, // Immediate Or Cancel
        FOK  // Fill Or Kill
    }

    @PrePersist
    @PreUpdate
    private void calculateRemainingAmount() {
        if (this.remainingAmount == null) {
            this.remainingAmount = this.amount.subtract(this.filledAmount);
        }
    }
}
