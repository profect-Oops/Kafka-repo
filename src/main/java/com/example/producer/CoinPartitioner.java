package com.example.producer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.codec.digest.MurmurHash3;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

public class CoinPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                         Object value, byte[] valueBytes, Cluster cluster) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        int numPartitions = cluster.partitionCountForTopic(topic);

        // Apache Commons의 MurmurHash3 사용
        int hash = MurmurHash3.hash32x86(
                key.toString().getBytes(StandardCharsets.UTF_8)
        );

        return Math.abs(hash % numPartitions);
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> configs) {}
}


