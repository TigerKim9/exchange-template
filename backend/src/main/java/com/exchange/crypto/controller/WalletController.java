package com.exchange.crypto.controller;

import com.exchange.crypto.dto.DepositRequest;
import com.exchange.crypto.dto.WalletDto;
import com.exchange.crypto.dto.WithdrawRequest;
import com.exchange.crypto.model.Transaction;
import com.exchange.crypto.security.UserDetailsImpl;
import com.exchange.crypto.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    public ResponseEntity<List<WalletDto>> getUserWallets(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<WalletDto> wallets = walletService.getUserWallets(userDetails.getId());
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{currency}")
    public ResponseEntity<WalletDto> getWallet(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String currency) {
        try {
            WalletDto wallet = walletService.getWallet(userDetails.getId(), currency);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create/{currency}")
    public ResponseEntity<?> createWallet(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String currency) {
        try {
            WalletDto wallet = walletService.createWallet(userDetails.getId(), currency);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody DepositRequest request) {
        try {
            Transaction transaction = walletService.deposit(userDetails.getId(), request);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody WithdrawRequest request) {
        try {
            Transaction transaction = walletService.withdraw(userDetails.getId(), request);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
