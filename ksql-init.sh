#!/bin/bash

echo "Waiting for KSQL server to be ready..."
while ! curl -s http://ksqldb-server:8088/info | grep -q "KsqlServerInfo"; do
  echo "KSQLDB server is not ready yet. Retrying in 5 seconds..."
  sleep 5
done

echo "KSQLDB server is ready. Waiting extra time for full initialization..."
sleep 10  

echo "Creating KSQL tables..."
ksql http://ksqldb-server:8088 <<EOF
CREATE TABLE IF NOT EXISTS alert_setting_table (
    keystring VARCHAR PRIMARY KEY,
    user_id VARCHAR,
    coin_id VARCHAR,
    alert_price DECIMAL(20,8),
    alert_active BOOLEAN,
    ticker VARCHAR,
    email VARCHAR,
    alert_type BOOLEAN
) WITH (
    kafka_topic = 'user-alert-settings',
    value_format = 'JSON',
    key_format = 'KAFKA'
);

CREATE TABLE IF NOT EXISTS coin_price_table (
    coinName VARCHAR PRIMARY KEY,
    code VARCHAR,
    price DECIMAL(20,8),
    changeRate DOUBLE
) WITH (
    kafka_topic='coin-price-realtime',
    value_format='JSON',
    key_format='KAFKA'
);

CREATE TABLE IF NOT EXISTS alert_table_above
WITH (
    KAFKA_TOPIC = 'alert-table-above',
    VALUE_FORMAT = 'JSON',
    KEY_FORMAT = 'KAFKA'
) AS
SELECT
    a.keystring AS keystring,
    a.user_id AS user_id,
    a.alert_price AS alert_price,
    a.ticker AS ticker,
    a.email AS email,
    c.price AS current_price,
    TRUE AS alert_type
FROM alert_setting_table a
JOIN coin_price_table c
ON a.ticker = c.coinName
WHERE a.alert_active = TRUE
AND a.alert_type = TRUE
AND c.price > a.alert_price
EMIT CHANGES;

CREATE TABLE IF NOT EXISTS alert_table_below
WITH (
    KAFKA_TOPIC = 'alert-table-below',
    VALUE_FORMAT = 'JSON',
    KEY_FORMAT = 'KAFKA'
) AS
SELECT
    a.keystring AS keystring,
    a.user_id AS user_id,
    a.alert_price AS alert_price,
    a.ticker AS ticker,
    a.email AS email,
    c.price AS current_price,
    FALSE AS alert_type
FROM alert_setting_table a
JOIN coin_price_table c
ON a.ticker = c.coinName
WHERE a.alert_active = TRUE
AND a.alert_type = FALSE
AND c.price < a.alert_price
EMIT CHANGES;
EOF

echo "KSQL tables created successfully."

