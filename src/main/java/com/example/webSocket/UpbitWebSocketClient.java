package com.example.webSocket;

import com.example.model.CoinCandle;
import com.example.model.CoinPrice;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class UpbitWebSocketClient {
    public static final String[] COIN_CODES = {
            "KRW-XRP", "KRW-ETH", "KRW-BERA",
            "KRW-USDT", "KRW-MOVE", "KRW-LAYER", "KRW-STPT",
            "KRW-GLM", "KRW-SOL"
    };
    private static final Logger logger = LoggerFactory.getLogger(UpbitWebSocketClient.class);
    private static final String WEBSOCKET_URL = "wss://api.upbit.com/websocket/v1";
    private static final int RECONNECT_DELAY = 5000; // 5초

    private final OkHttpClient client;
    private final Consumer<CoinPrice> priceHandler;
    private final Consumer<CoinCandle> candleHandler;
    private WebSocket webSocket;
    private boolean shouldReconnect = true;

    public UpbitWebSocketClient(Consumer<CoinPrice> priceHandler, Consumer<CoinCandle> candleHandler) {
        this.client = new OkHttpClient();
        this.priceHandler = priceHandler;
        this.candleHandler = candleHandler;
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(WEBSOCKET_URL)
                .build();

        webSocket = client.newWebSocket(request, new UpbitWebSocketListener(priceHandler, candleHandler) {
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                reconnect();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                reconnect();
            }
        });
    }

    private void reconnect() {
        if (!shouldReconnect) return;

        try {
            Thread.sleep(RECONNECT_DELAY);
            logger.info("Attempting to reconnect...");
            connect();
        } catch (InterruptedException e) {
            logger.error("Reconnection interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public void close() {
        shouldReconnect = false;
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
            client.dispatcher().executorService().shutdown();
        }
    }
}

class UpbitWebSocketListener extends WebSocketListener {
    private static final Logger logger = LoggerFactory.getLogger(UpbitWebSocketListener.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Consumer<CoinPrice> priceHandler;
    private final Consumer<CoinCandle> candleHandler;

    public UpbitWebSocketListener(Consumer<CoinPrice> priceHandler, Consumer<CoinCandle> candleHandler) {
        this.priceHandler = priceHandler;
        this.candleHandler = candleHandler;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        logger.info("WebSocket Opened");
        try {
            String message = getMessage();
            webSocket.send(message);
        } catch (IOException e) {
            logger.error("Error creating message", e);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        try {
            JsonNode jsonNode = objectMapper.readTree(bytes.utf8());
            if (!jsonNode.has("type")) {
                logger.warn("Received message without type field");
                return;
            }
            String type = jsonNode.get("type").asText();

            try {
                if (type.startsWith("candle")) {
                    CoinCandle candle = processCandle(jsonNode);
                    candleHandler.accept(candle);
                } else if ("ticker".equals(type)) {
                    CoinPrice price = processTicker(jsonNode);
                    priceHandler.accept(price);
                }
            } catch (NullPointerException e) {
                logger.error("Missing required fields in message: {}", jsonNode.toString(), e);
            }
        } catch (IOException e) {
            logger.error("Error processing message: {}", bytes.utf8(), e);
        }
    }

    private CoinCandle processCandle(JsonNode jsonNode) {
        return new CoinCandle(
                jsonNode.get("code").asText(),
                jsonNode.get("opening_price").asDouble(),
                jsonNode.get("high_price").asDouble(),
                jsonNode.get("low_price").asDouble(),
                jsonNode.get("trade_price").asDouble(),
                jsonNode.get("candle_acc_trade_price").asDouble(),
                jsonNode.get("timestamp").asLong()
        );
    }

    private CoinPrice processTicker(JsonNode jsonNode) {
        return new CoinPrice(
                jsonNode.get("code").asText(),
                jsonNode.get("trade_price").asDouble(),
                jsonNode.get("signed_change_rate").asDouble()
        );
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        logger.info("WebSocket Closed: {} / {}", code, reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        logger.error("WebSocket Error", t);
    }

    private String getMessage() throws IOException {
        List<Map<String, Object>> message = new ArrayList<>();

        // Ticket
        message.add(Collections.singletonMap("ticket", UUID.randomUUID().toString()));

        // Ticker
        Map<String, Object> ticker = new HashMap<>();
        ticker.put("type", "ticker");
        ticker.put("codes", Arrays.asList(UpbitWebSocketClient.COIN_CODES));
        message.add(ticker);

        // Candle
        Map<String, Object> candle = new HashMap<>();
        candle.put("type", "candle.1s");
        candle.put("codes", Arrays.asList(UpbitWebSocketClient.COIN_CODES));
        message.add(candle);

        // Format
        message.add(Collections.singletonMap("format", "DEFAULT"));

        return objectMapper.writeValueAsString(message);
    }
}