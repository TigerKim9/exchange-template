package com.exchange.crypto.controller;

import com.exchange.crypto.model.Transaction;
import com.exchange.crypto.repository.TransactionRepository;
import com.exchange.crypto.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(transactionRepository.findByUserId(userDetails.getId()));
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Transaction>> getUserTransactionsPaged(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(transactionRepository.findByUserId(userDetails.getId(), pageable));
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<Transaction>> getUserTransactionsByCurrency(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String currency) {
        return ResponseEntity.ok(transactionRepository.findByUserIdAndCurrency(
                userDetails.getId(), currency));
    }
}
