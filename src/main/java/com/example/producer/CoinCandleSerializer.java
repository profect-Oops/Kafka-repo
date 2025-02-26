package com.example.producer;

import com.example.model.CoinCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoinCandleSerializer implements Serializer<CoinCandle> {

    public static final Logger logger = LoggerFactory.getLogger(CoinCandleSerializer.class.getName());

    @Override
    public byte[] serialize(String topic, CoinCandle coinCandle) {

        ObjectMapper objectMapper = new ObjectMapper();

        byte[] serializedCoinCandle= null;
        try {
            serializedCoinCandle = objectMapper.writeValueAsBytes(coinCandle);
        } catch (JsonProcessingException e) {
            logger.error("Candle Json Serialization exception" + e.getMessage());
        }

        return serializedCoinCandle;
    }
}
