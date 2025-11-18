package com.exchange.crypto.service;

import com.exchange.crypto.dto.DepositRequest;
import com.exchange.crypto.dto.WalletDto;
import com.exchange.crypto.dto.WithdrawRequest;
import com.exchange.crypto.model.Transaction;
import com.exchange.crypto.model.User;
import com.exchange.crypto.model.Wallet;
import com.exchange.crypto.repository.TransactionRepository;
import com.exchange.crypto.repository.UserRepository;
import com.exchange.crypto.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<WalletDto> getUserWallets(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public WalletDto getWallet(Long userId, String currency) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return convertToDto(wallet);
    }

    @Transactional
    public WalletDto createWallet(Long userId, String currency) {
        if (walletRepository.findByUserIdAndCurrency(userId, currency).isPresent()) {
            throw new RuntimeException("Wallet already exists for this currency");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency(currency);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setLockedBalance(BigDecimal.ZERO);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setWalletAddress(generateWalletAddress(currency));

        wallet = walletRepository.save(wallet);
        return convertToDto(wallet);
    }

    @Transactional
    public Transaction deposit(Long userId, DepositRequest request) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, request.getCurrency())
                .orElseGet(() -> {
                    createWallet(userId, request.getCurrency());
                    return walletRepository.findByUserIdAndCurrency(userId, request.getCurrency())
                            .orElseThrow(() -> new RuntimeException("Failed to create wallet"));
                });

        User user = wallet.getUser();

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        wallet.updateAvailableBalance();
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setCurrency(request.getCurrency());
        transaction.setAmount(request.getAmount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setTransactionHash(request.getTransactionHash());
        transaction.setCompletedAt(LocalDateTime.now());
        transaction.setDescription("Deposit " + request.getAmount() + " " + request.getCurrency());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(Long userId, WithdrawRequest request) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, request.getCurrency())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        User user = wallet.getUser();

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        wallet.updateAvailableBalance();
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setCurrency(request.getCurrency());
        transaction.setAmount(request.getAmount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setWalletAddress(request.getWalletAddress());
        transaction.setCompletedAt(LocalDateTime.now());
        transaction.setDescription("Withdrawal " + request.getAmount() + " " + request.getCurrency());

        return transactionRepository.save(transaction);
    }

    private WalletDto convertToDto(Wallet wallet) {
        return new WalletDto(
                wallet.getId(),
                wallet.getCurrency(),
                wallet.getBalance(),
                wallet.getLockedBalance(),
                wallet.getAvailableBalance(),
                wallet.getWalletAddress()
        );
    }

    private String generateWalletAddress(String currency) {
        // Simple wallet address generation (in production, use proper address generation)
        return currency + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
}
