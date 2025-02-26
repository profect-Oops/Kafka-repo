package com.example;

import com.example.consumer.AboveAlertConsumer;
import com.example.producer.CoinCandleProducer;
import com.example.producer.CoinPriceProducer;
import com.example.producer.UserAlertSettingProducer;
import com.example.webSocket.UpbitWebSocketClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int NUM_CONSUMERS = 6;  // 파티션 개수와 동일하게 설정
    private static final String ALERT_TOPIC = "alert-table-above";
    private static final String ALERT_SETTING_TOPIC = "user-alert-settings";
    private static final String GROUP_ID = "alert-consumer-group";

    public static void main(String[] args) {
        String bootstrapServers = "192.168.56.101:29092,192.168.56.101:29093,192.168.56.101:29094";

        // Kafka Producers
        CoinPriceProducer priceProducer = new CoinPriceProducer(bootstrapServers, "coin-price-realtime");
        CoinCandleProducer candleProducer = new CoinCandleProducer(bootstrapServers, "coin-candle-realtime");
        UserAlertSettingProducer alertSettingProducer = new UserAlertSettingProducer(bootstrapServers, ALERT_SETTING_TOPIC);

        // WebSocket Client
        UpbitWebSocketClient webSocketClient = new UpbitWebSocketClient(
                priceProducer::send,  // price handler
                candleProducer::send   // candle handler
        );

        // ✅ ExecutorService로 스레드 풀 관리 (Runnable 활용)
        ExecutorService consumerExecutor = Executors.newFixedThreadPool(NUM_CONSUMERS);
        List<AboveAlertConsumer> consumers = new ArrayList<>();

        for (int i = 0; i < NUM_CONSUMERS; i++) {
            AboveAlertConsumer consumer = new AboveAlertConsumer(bootstrapServers, ALERT_TOPIC, GROUP_ID, alertSettingProducer);
            consumers.add(consumer);

            consumerExecutor.submit(() -> { // ✅ Runnable 사용
                try {
                    consumer.pollAndProcess();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    consumer.close();
                }
            });
        }

        // WebSocket 실행
        webSocketClient.connect();

        // Shutdown hook 추가
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gracefully...");

            webSocketClient.close();
            priceProducer.close();
            candleProducer.close();
            alertSettingProducer.close();

            // ✅ 컨슈머 안전 종료
            consumers.forEach(AboveAlertConsumer::close);
            consumerExecutor.shutdown();
        }));
    }
}
