package com.exchange.crypto.controller;

import com.exchange.crypto.model.TradingPair;
import com.exchange.crypto.model.User;
import com.exchange.crypto.repository.OrderRepository;
import com.exchange.crypto.repository.TradingPairRepository;
import com.exchange.crypto.repository.TransactionRepository;
import com.exchange.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TradingPairRepository tradingPairRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/users/paged")
    public ResponseEntity<Page<User>> getAllUsersPaged(Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(true);
                    userRepository.save(user);
                    return ResponseEntity.ok("User enabled successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(false);
                    userRepository.save(user);
                    return ResponseEntity.ok("User disabled successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalTransactions", transactionRepository.count());
        stats.put("totalTradingPairs", tradingPairRepository.count());
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/trading-pairs")
    public ResponseEntity<?> createTradingPair(@RequestBody TradingPair tradingPair) {
        try {
            TradingPair saved = tradingPairRepository.save(tradingPair);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/trading-pairs/{id}")
    public ResponseEntity<?> updateTradingPair(@PathVariable Long id, @RequestBody TradingPair tradingPair) {
        return tradingPairRepository.findById(id)
                .map(existing -> {
                    existing.setCurrentPrice(tradingPair.getCurrentPrice());
                    existing.setHigh24h(tradingPair.getHigh24h());
                    existing.setLow24h(tradingPair.getLow24h());
                    existing.setVolume24h(tradingPair.getVolume24h());
                    existing.setPriceChange24h(tradingPair.getPriceChange24h());
                    existing.setPriceChangePercent24h(tradingPair.getPriceChangePercent24h());
                    existing.setActive(tradingPair.getActive());
                    TradingPair saved = tradingPairRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
