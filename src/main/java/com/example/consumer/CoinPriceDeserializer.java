package com.example.consumer;

import com.example.model.CoinPrice;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoinPriceDeserializer implements Deserializer<CoinPrice> {

    public static final Logger logger = LoggerFactory.getLogger(CoinPriceDeserializer.class.getName());

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CoinPrice deserialize(String topic, byte[] bytes) {
        CoinPrice coinPrice = null;

        try {
            coinPrice = objectMapper.readValue(bytes, CoinPrice.class);
        } catch (IOException e) {
            logger.error("CoinPrice Deserialization Error" + e.getMessage());
        }

        return coinPrice;
    }

}
