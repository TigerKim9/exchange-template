package com.exchange.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {
    private Long id;
    private String currency;
    private BigDecimal balance;
    private BigDecimal lockedBalance;
    private BigDecimal availableBalance;
    private String walletAddress;
}
