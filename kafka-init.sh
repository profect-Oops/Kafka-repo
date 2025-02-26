#!/bin/bash

echo "Waiting for Kafka to be ready..."
while ! nc -z kafka-1 9092; do   
  sleep 1
done
echo "Kafka is ready."

echo "Creating Kafka topics..."
kafka-topics --create --bootstrap-server kafka-1:9092 --topic coin-price-realtime --partitions 6 --replication-factor 2 --config retention.ms=600000
kafka-topics --create --bootstrap-server kafka-1:9092 --topic coin-candle-realtime --partitions 6 --replication-factor 2 --config retention.ms=600000
kafka-topics --create --bootstrap-server kafka-1:9092 --topic user-alert-settings --partitions 6 --replication-factor 2 --config cleanup.policy=compact

echo "Kafka topics created successfully."

