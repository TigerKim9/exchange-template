package com.exchange.crypto.controller;

import com.exchange.crypto.dto.CreateOrderRequest;
import com.exchange.crypto.dto.OrderDto;
import com.exchange.crypto.model.Order;
import com.exchange.crypto.model.TradingPair;
import com.exchange.crypto.security.UserDetailsImpl;
import com.exchange.crypto.service.OrderService;
import com.exchange.crypto.service.TradingPairService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TradingPairService tradingPairService;

    @GetMapping("/pairs")
    public ResponseEntity<List<TradingPair>> getTradingPairs() {
        return ResponseEntity.ok(tradingPairService.getActiveTradingPairs());
    }

    @GetMapping("/pairs/{id}")
    public ResponseEntity<TradingPair> getTradingPair(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tradingPairService.getTradingPair(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(userDetails.getId(), request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getUserOrders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getId()));
    }

    @GetMapping("/orders/paged")
    public ResponseEntity<Page<OrderDto>> getUserOrdersPaged(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getUserOrdersPaged(userDetails.getId(), pageable));
    }

    @GetMapping("/orders/active/{tradingPairId}")
    public ResponseEntity<List<OrderDto>> getActiveOrders(@PathVariable Long tradingPairId) {
        return ResponseEntity.ok(orderService.getActiveOrders(tradingPairId));
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<?> cancelOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId, userDetails.getId());
            return ResponseEntity.ok("Order cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
