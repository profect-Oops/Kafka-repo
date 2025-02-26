package com.example.consumer;

import com.example.model.CoinCandle;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoinCandleDeserializer implements Deserializer<CoinCandle> {

    public static final Logger logger = LoggerFactory.getLogger(CoinCandleDeserializer.class.getName());

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CoinCandle deserialize(String topic, byte[] bytes) {
        CoinCandle coinCandle = null;

        try {
            coinCandle = objectMapper.readValue(bytes, CoinCandle.class);
        } catch (IOException e) {
            logger.error("CoinCandle Deserialization Error" + e.getMessage());
        }

        return coinCandle;
    }

}
