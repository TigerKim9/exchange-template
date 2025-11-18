package com.exchange.crypto.repository;

import com.exchange.crypto.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    List<Transaction> findByUserIdAndCurrency(Long userId, String currency);
}
