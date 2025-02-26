package com.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class SimpleProducer {
    public static void main(String[] args) {
        // Kafka 프로듀서 설정
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "192.168.56.101:29092");  // Kafka 브로커 주소
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // 프로듀서 인스턴스 생성
        Producer<String, String> producer = new KafkaProducer<>(properties);

        try {
            // 메시지 전송
            String topic = "hello";
            String message = "Hello, Kafka!";

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
            producer.send(record);

            System.out.println("Message sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 프로듀서 종료
            producer.close();
        }
    }
}