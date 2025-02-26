package com.example.producer;

import com.example.model.CoinPrice;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CoinPriceProducer {
    private static final Logger logger = LoggerFactory.getLogger(CoinPriceProducer.class);
    private final KafkaProducer<String, CoinPrice> producer;
    private final String topic;

    public CoinPriceProducer(String bootstrapServers, String topic) {
        this.topic = topic;
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CoinPriceSerializer.class.getName());

        this.producer = new KafkaProducer<>(props);
        logger.info("CoinPrice Producer initialized");
    }

    public void send(CoinPrice coinPrice) {
        ProducerRecord<String, CoinPrice> record =
                new ProducerRecord<>(topic, coinPrice.getCode(), coinPrice);

        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                logger.info("Received metadata. Topic: {}, Price: {}, Code: {}, Partition: {}, Offset: {}, Timestamp: {}",
                        metadata.topic(),record.value().getPrice(),record.key() ,metadata.partition(), metadata.offset(), metadata.timestamp());
            } else {
                logger.error("Error while producing", exception);
            }
        });
    }

    public void close() {
        producer.close();
        logger.info("Producer closed");
    }
}