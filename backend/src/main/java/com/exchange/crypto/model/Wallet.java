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
@Table(name = "wallets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "currency"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String currency; // BTC, ETH, USD, USDT, etc.

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "locked_balance", nullable = false, precision = 20, scale = 8)
    private BigDecimal lockedBalance = BigDecimal.ZERO; // For pending orders

    @Column(name = "available_balance", nullable = false, precision = 20, scale = 8)
    private BigDecimal availableBalance = BigDecimal.ZERO; // balance - lockedBalance

    @Column(name = "wallet_address")
    private String walletAddress; // For deposits/withdrawals

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateAvailableBalance() {
        this.availableBalance = this.balance.subtract(this.lockedBalance);
    }
}
