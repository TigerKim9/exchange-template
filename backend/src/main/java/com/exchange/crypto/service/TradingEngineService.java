package com.exchange.crypto.service;

import com.exchange.crypto.model.Order;
import com.exchange.crypto.model.Transaction;
import com.exchange.crypto.model.Wallet;
import com.exchange.crypto.repository.OrderRepository;
import com.exchange.crypto.repository.TransactionRepository;
import com.exchange.crypto.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class TradingEngineService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public void matchOrder(Order newOrder) {
        if (newOrder.getType() == Order.OrderType.MARKET) {
            matchMarketOrder(newOrder);
        } else if (newOrder.getType() == Order.OrderType.LIMIT) {
            matchLimitOrder(newOrder);
        }
    }

    private void matchMarketOrder(Order marketOrder) {
        // Get opposite orders
        List<Order.OrderStatus> activeStatuses = Arrays.asList(
                Order.OrderStatus.PENDING,
                Order.OrderStatus.PARTIALLY_FILLED
        );

        List<Order> oppositeOrders = orderRepository.findActiveOrdersByTradingPair(
                marketOrder.getTradingPair().getId(),
                activeStatuses
        );

        // Filter by opposite side and sort by price
        oppositeOrders = oppositeOrders.stream()
                .filter(o -> o.getSide() != marketOrder.getSide())
                .filter(o -> o.getType() == Order.OrderType.LIMIT)
                .sorted((o1, o2) -> {
                    if (marketOrder.getSide() == Order.OrderSide.BUY) {
                        return o1.getPrice().compareTo(o2.getPrice()); // Buy at lowest price
                    } else {
                        return o2.getPrice().compareTo(o1.getPrice()); // Sell at highest price
                    }
                })
                .toList();

        // Match orders
        for (Order oppositeOrder : oppositeOrders) {
            if (marketOrder.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            executeMatch(marketOrder, oppositeOrder, oppositeOrder.getPrice());
        }

        // Update market order status
        updateOrderStatus(marketOrder);
        orderRepository.save(marketOrder);
    }

    private void matchLimitOrder(Order limitOrder) {
        // Get opposite orders
        List<Order.OrderStatus> activeStatuses = Arrays.asList(
                Order.OrderStatus.PENDING,
                Order.OrderStatus.PARTIALLY_FILLED
        );

        List<Order> oppositeOrders = orderRepository.findActiveOrdersByTradingPair(
                limitOrder.getTradingPair().getId(),
                activeStatuses
        );

        // Filter by opposite side, matching price, and sort
        oppositeOrders = oppositeOrders.stream()
                .filter(o -> o.getSide() != limitOrder.getSide())
                .filter(o -> {
                    if (limitOrder.getSide() == Order.OrderSide.BUY) {
                        return o.getPrice().compareTo(limitOrder.getPrice()) <= 0;
                    } else {
                        return o.getPrice().compareTo(limitOrder.getPrice()) >= 0;
                    }
                })
                .sorted((o1, o2) -> {
                    if (limitOrder.getSide() == Order.OrderSide.BUY) {
                        return o1.getPrice().compareTo(o2.getPrice());
                    } else {
                        return o2.getPrice().compareTo(o1.getPrice());
                    }
                })
                .toList();

        // Match orders
        for (Order oppositeOrder : oppositeOrders) {
            if (limitOrder.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            executeMatch(limitOrder, oppositeOrder, oppositeOrder.getPrice());
        }

        // Update limit order status
        updateOrderStatus(limitOrder);
        orderRepository.save(limitOrder);
    }

    private void executeMatch(Order order1, Order order2, BigDecimal matchPrice) {
        BigDecimal matchAmount = order1.getRemainingAmount().min(order2.getRemainingAmount());

        // Update order 1
        order1.setFilledAmount(order1.getFilledAmount().add(matchAmount));
        order1.setRemainingAmount(order1.getRemainingAmount().subtract(matchAmount));

        // Update order 2
        order2.setFilledAmount(order2.getFilledAmount().add(matchAmount));
        order2.setRemainingAmount(order2.getRemainingAmount().subtract(matchAmount));

        // Calculate fee (0.1% default)
        BigDecimal feeRate = order1.getTradingPair().getTradingFee();
        BigDecimal total = matchPrice.multiply(matchAmount);
        BigDecimal fee = total.multiply(feeRate).setScale(8, RoundingMode.HALF_UP);

        order1.setFee((order1.getFee() == null ? BigDecimal.ZERO : order1.getFee()).add(fee));
        order2.setFee((order2.getFee() == null ? BigDecimal.ZERO : order2.getFee()).add(fee));

        // Update wallets
        updateWalletsAfterMatch(order1, order2, matchAmount, matchPrice, fee);

        // Update order statuses
        updateOrderStatus(order1);
        updateOrderStatus(order2);

        orderRepository.save(order1);
        orderRepository.save(order2);

        // Create transactions
        createTradeTransactions(order1, order2, matchAmount, matchPrice, fee);
    }

    private void updateWalletsAfterMatch(Order buyOrder, Order sellOrder,
                                         BigDecimal amount, BigDecimal price, BigDecimal fee) {
        Order buyer = buyOrder.getSide() == Order.OrderSide.BUY ? buyOrder : sellOrder;
        Order seller = buyOrder.getSide() == Order.OrderSide.SELL ? buyOrder : sellOrder;

        String baseCurrency = buyer.getTradingPair().getBaseCurrency();
        String quoteCurrency = buyer.getTradingPair().getQuoteCurrency();

        BigDecimal total = price.multiply(amount);

        // Buyer: Give quote currency, receive base currency
        Wallet buyerQuoteWallet = walletRepository.findByUserIdAndCurrency(
                buyer.getUser().getId(), quoteCurrency
        ).orElseThrow();

        Wallet buyerBaseWallet = walletRepository.findByUserIdAndCurrency(
                buyer.getUser().getId(), baseCurrency
        ).orElse(null);

        if (buyerBaseWallet == null) {
            buyerBaseWallet = createWallet(buyer.getUser().getId(), baseCurrency);
        }

        buyerQuoteWallet.setLockedBalance(buyerQuoteWallet.getLockedBalance().subtract(total));
        buyerQuoteWallet.updateAvailableBalance();

        buyerBaseWallet.setBalance(buyerBaseWallet.getBalance().add(amount).subtract(fee));
        buyerBaseWallet.updateAvailableBalance();

        // Seller: Give base currency, receive quote currency
        Wallet sellerBaseWallet = walletRepository.findByUserIdAndCurrency(
                seller.getUser().getId(), baseCurrency
        ).orElseThrow();

        Wallet sellerQuoteWallet = walletRepository.findByUserIdAndCurrency(
                seller.getUser().getId(), quoteCurrency
        ).orElse(null);

        if (sellerQuoteWallet == null) {
            sellerQuoteWallet = createWallet(seller.getUser().getId(), quoteCurrency);
        }

        sellerBaseWallet.setLockedBalance(sellerBaseWallet.getLockedBalance().subtract(amount));
        sellerBaseWallet.updateAvailableBalance();

        sellerQuoteWallet.setBalance(sellerQuoteWallet.getBalance().add(total).subtract(fee));
        sellerQuoteWallet.updateAvailableBalance();

        walletRepository.save(buyerQuoteWallet);
        walletRepository.save(buyerBaseWallet);
        walletRepository.save(sellerBaseWallet);
        walletRepository.save(sellerQuoteWallet);
    }

    private void createTradeTransactions(Order order1, Order order2,
                                          BigDecimal amount, BigDecimal price, BigDecimal fee) {
        // Create transaction for order 1
        Transaction tx1 = new Transaction();
        tx1.setUser(order1.getUser());
        tx1.setOrder(order1);
        tx1.setType(order1.getSide() == Order.OrderSide.BUY ?
                Transaction.TransactionType.TRADE_BUY : Transaction.TransactionType.TRADE_SELL);
        tx1.setCurrency(order1.getTradingPair().getBaseCurrency());
        tx1.setAmount(amount);
        tx1.setFee(fee);
        tx1.setStatus(Transaction.TransactionStatus.COMPLETED);
        tx1.setCompletedAt(LocalDateTime.now());
        tx1.setDescription("Trade: " + order1.getSide() + " " + amount + " " +
                order1.getTradingPair().getSymbol() + " @ " + price);

        transactionRepository.save(tx1);

        // Create transaction for order 2
        Transaction tx2 = new Transaction();
        tx2.setUser(order2.getUser());
        tx2.setOrder(order2);
        tx2.setType(order2.getSide() == Order.OrderSide.BUY ?
                Transaction.TransactionType.TRADE_BUY : Transaction.TransactionType.TRADE_SELL);
        tx2.setCurrency(order2.getTradingPair().getBaseCurrency());
        tx2.setAmount(amount);
        tx2.setFee(fee);
        tx2.setStatus(Transaction.TransactionStatus.COMPLETED);
        tx2.setCompletedAt(LocalDateTime.now());
        tx2.setDescription("Trade: " + order2.getSide() + " " + amount + " " +
                order2.getTradingPair().getSymbol() + " @ " + price);

        transactionRepository.save(tx2);
    }

    private void updateOrderStatus(Order order) {
        if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(Order.OrderStatus.FILLED);
            order.setFilledAt(LocalDateTime.now());
        } else if (order.getFilledAmount().compareTo(BigDecimal.ZERO) > 0) {
            order.setStatus(Order.OrderStatus.PARTIALLY_FILLED);
        }
    }

    private Wallet createWallet(Long userId, String currency) {
        Wallet wallet = new Wallet();
        wallet.setUser(userRepository.findById(userId).orElseThrow());
        wallet.setCurrency(currency);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setLockedBalance(BigDecimal.ZERO);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setWalletAddress(currency + "_" + System.currentTimeMillis());
        return walletRepository.save(wallet);
    }

    @Autowired
    private com.exchange.crypto.repository.UserRepository userRepository;
}
