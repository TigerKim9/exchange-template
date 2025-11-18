package com.exchange.crypto.repository;

import com.exchange.crypto.model.Order;
import com.exchange.crypto.model.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByTradingPairIdAndStatus(Long tradingPairId, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.tradingPair.id = :tradingPairId AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByTradingPair(Long tradingPairId, List<OrderStatus> statuses);
}
