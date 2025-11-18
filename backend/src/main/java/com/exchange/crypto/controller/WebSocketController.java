package com.exchange.crypto.controller;

import com.exchange.crypto.dto.MarketUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/market")
    @SendTo("/topic/market")
    public MarketUpdate sendMarketUpdate(MarketUpdate update) {
        return update;
    }

    public void broadcastMarketUpdate(MarketUpdate update) {
        messagingTemplate.convertAndSend("/topic/market/" + update.getSymbol(), update);
    }

    public void broadcastOrderBookUpdate(String symbol, Object orderBook) {
        messagingTemplate.convertAndSend("/topic/orderbook/" + symbol, orderBook);
    }
}
