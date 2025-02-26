package com.example.consumer;

import com.example.producer.UserAlertSettingProducer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class AboveAlertConsumer {
    private static final Logger logger = LoggerFactory.getLogger(AboveAlertConsumer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaConsumer<String, String> consumer;
    private final String topic;
    private final UserAlertSettingProducer producer;
    private final AtomicBoolean running = new AtomicBoolean(true); // ✅ 종료 플래그 추가

    public AboveAlertConsumer(String bootstrapServers, String topic, String groupId, UserAlertSettingProducer producer) {
        this.topic = topic;
        this.producer = producer;

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // ✅ 수동 커밋으로 변경

        this.consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        logger.info("AboveAlertConsumer initialized and subscribed to topic: {}", topic);
    }

    public void pollAndProcess() {
        try {
            while (running.get()) { // ✅ 종료 플래그 활용
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    if (record.value() == null) {
                        continue;
                    }

                    processAlert(record.key(), record.value());
                }

                consumer.commitSync(); // ✅ 정상적으로 처리된 메시지만 커밋
            }
        } catch (WakeupException e) {
            if (running.get()) throw e; // WakeupException은 정상 종료 과정에서 발생할 수 있음
        } catch (Exception e) {
            logger.error("❌ Error during polling", e);
        } finally {
            consumer.close();
            logger.info("AboveAlertConsumer closed");
        }
    }

    private void processAlert(String key, String alertMessage) {
        logger.info("📢 [ALERT] {}", alertMessage);

        try {
            JsonNode jsonNode = objectMapper.readTree(alertMessage);

            // ✅ 기존 JSON을 수정하여 새로운 JSON을 만들지 않음
            ((ObjectNode) jsonNode).put("alert_active", false);

            producer.sendMessage(key, objectMapper.writeValueAsString(jsonNode));
            logger.info("✅ Updated Alert Sent to user-alert-setting: {}", jsonNode);

        } catch (Exception e) {
            logger.error("❌ Error parsing alert message: {}", alertMessage, e);
        }
    }

    public void close() {
        running.set(false); // ✅ 종료 신호 설정
        consumer.wakeup(); // ✅ poll() 즉시 중단
    }
}
