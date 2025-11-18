package com.exchange.crypto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(precision = 20, scale = 8)
    private BigDecimal fee;

    @Column(name = "balance_before", precision = 20, scale = 8)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 20, scale = 8)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "transaction_hash")
    private String transactionHash; // For blockchain transactions

    @Column(name = "wallet_address")
    private String walletAddress; // Deposit/withdrawal address

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRADE_BUY,
        TRADE_SELL,
        FEE,
        REFUND,
        BONUS
    }

    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
