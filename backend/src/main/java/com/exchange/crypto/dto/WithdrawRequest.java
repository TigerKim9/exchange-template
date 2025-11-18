package com.exchange.crypto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequest {
    @NotBlank
    private String currency;

    @NotNull
    @DecimalMin(value = "0.00000001")
    private BigDecimal amount;

    @NotBlank
    private String walletAddress;
}
