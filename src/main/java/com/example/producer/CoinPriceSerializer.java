package com.example.producer;

import com.example.model.CoinPrice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoinPriceSerializer implements Serializer<CoinPrice> {

    public static final Logger logger = LoggerFactory.getLogger(CoinPriceSerializer.class.getName());

    @Override
    public byte[] serialize(String topic, CoinPrice coinPrice) {

        ObjectMapper objectMapper = new ObjectMapper();

        byte[] serializedCoinPrice = null;
        try {
            serializedCoinPrice = objectMapper.writeValueAsBytes(coinPrice);
        } catch (JsonProcessingException e) {
            logger.error("Price Json Serialization exception" + e.getMessage());
        }

        return serializedCoinPrice;
    }
}
