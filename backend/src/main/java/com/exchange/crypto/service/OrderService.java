package com.exchange.crypto.service;

import com.exchange.crypto.dto.CreateOrderRequest;
import com.exchange.crypto.dto.OrderDto;
import com.exchange.crypto.model.Order;
import com.exchange.crypto.model.TradingPair;
import com.exchange.crypto.model.User;
import com.exchange.crypto.model.Wallet;
import com.exchange.crypto.repository.OrderRepository;
import com.exchange.crypto.repository.TradingPairRepository;
import com.exchange.crypto.repository.UserRepository;
import com.exchange.crypto.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TradingPairRepository tradingPairRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TradingEngineService tradingEngineService;

    @Transactional
    public Order createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TradingPair tradingPair = tradingPairRepository.findById(request.getTradingPairId())
                .orElseThrow(() -> new RuntimeException("Trading pair not found"));

        // Validate and lock funds
        String currency = request.getSide() == Order.OrderSide.BUY ?
                tradingPair.getQuoteCurrency() : tradingPair.getBaseCurrency();

        BigDecimal requiredAmount = request.getSide() == Order.OrderSide.BUY ?
                request.getPrice().multiply(request.getAmount()) : request.getAmount();

        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found for currency: " + currency));

        if (wallet.getAvailableBalance().compareTo(requiredAmount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Lock funds
        wallet.setLockedBalance(wallet.getLockedBalance().add(requiredAmount));
        wallet.updateAvailableBalance();
        walletRepository.save(wallet);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setTradingPair(tradingPair);
        order.setType(request.getType());
        order.setSide(request.getSide());
        order.setPrice(request.getPrice());
        order.setAmount(request.getAmount());
        order.setRemainingAmount(request.getAmount());
        order.setFilledAmount(BigDecimal.ZERO);
        order.setTotal(request.getPrice().multiply(request.getAmount()));
        order.setStatus(Order.OrderStatus.PENDING);
        order.setStopPrice(request.getStopPrice());
        order.setTimeInForce(Order.TimeInForce.GTC);

        order = orderRepository.save(order);

        // Try to match order
        tradingEngineService.matchOrder(order);

        return order;
    }

    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<OrderDto> getUserOrdersPaged(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    public List<OrderDto> getActiveOrders(Long tradingPairId) {
        List<Order.OrderStatus> activeStatuses = Arrays.asList(
                Order.OrderStatus.PENDING,
                Order.OrderStatus.PARTIALLY_FILLED
        );
        return orderRepository.findActiveOrdersByTradingPair(tradingPairId, activeStatuses)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (order.getStatus() == Order.OrderStatus.FILLED ||
                order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel this order");
        }

        // Unlock funds
        String currency = order.getSide() == Order.OrderSide.BUY ?
                order.getTradingPair().getQuoteCurrency() : order.getTradingPair().getBaseCurrency();

        BigDecimal lockedAmount = order.getSide() == Order.OrderSide.BUY ?
                order.getPrice().multiply(order.getRemainingAmount()) : order.getRemainingAmount();

        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setLockedBalance(wallet.getLockedBalance().subtract(lockedAmount));
        wallet.updateAvailableBalance();
        walletRepository.save(wallet);

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private OrderDto convertToDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getTradingPair().getSymbol(),
                order.getType(),
                order.getSide(),
                order.getPrice(),
                order.getAmount(),
                order.getFilledAmount(),
                order.getRemainingAmount(),
                order.getTotal(),
                order.getFee(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
