package com.example.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class UserAlertSettingProducer {
    private static final Logger logger = LoggerFactory.getLogger(UserAlertSettingProducer.class);
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public UserAlertSettingProducer(String bootstrapServers, String topic) {
        this.topic = topic;

        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // 데이터 안전하게 전송

        this.producer = new KafkaProducer<>(props);
        logger.info("UserAlertSettingProducer initialized for topic: {}", topic);
    }

    public void sendMessage(String key, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                logger.info("✅ Successfully sent to {} (Partition={} Offset={})", topic, metadata.partition(), metadata.offset());
            } else {
                logger.error("❌ Error sending message to {}: {}", topic, exception.getMessage());
            }
        });

        producer.flush();
    }

    public void close() {
        producer.close();
        logger.info("UserAlertSettingProducer closed");
    }
}

