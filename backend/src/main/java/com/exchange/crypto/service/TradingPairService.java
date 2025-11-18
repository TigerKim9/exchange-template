package com.exchange.crypto.service;

import com.exchange.crypto.model.TradingPair;
import com.exchange.crypto.repository.TradingPairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradingPairService {

    @Autowired
    private TradingPairRepository tradingPairRepository;

    public List<TradingPair> getAllTradingPairs() {
        return tradingPairRepository.findAll();
    }

    public List<TradingPair> getActiveTradingPairs() {
        return tradingPairRepository.findByActiveTrue();
    }

    public TradingPair getTradingPair(Long id) {
        return tradingPairRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trading pair not found"));
    }

    public TradingPair getTradingPairBySymbol(String symbol) {
        return tradingPairRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Trading pair not found"));
    }
}
