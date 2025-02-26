package com.example.model;


import java.io.Serializable;

public class CoinCandle implements Serializable {
    private String code;
    private Double openingPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double tradePrice;
    private Double candleAccTradePrice;
    private Long timestamp;

    // 기본 생성자
    public CoinCandle() {
    }

    // 모든 필드를 포함한 생성자
    public CoinCandle(String code, Double openingPrice, Double highPrice,
                      Double lowPrice, Double tradePrice,
                      Double candleAccTradePrice, Long timestamp) {
        this.code = code;
        this.openingPrice = openingPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.tradePrice = tradePrice;
        this.candleAccTradePrice = candleAccTradePrice;
        this.timestamp = timestamp;
    }

    // Getter와 Setter
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(Double openingPrice) {
        this.openingPrice = openingPrice;
    }

    public Double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(Double highPrice) {
        this.highPrice = highPrice;
    }

    public Double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(Double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public Double getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(Double tradePrice) {
        this.tradePrice = tradePrice;
    }

    public Double getCandleAccTradePrice() {
        return candleAccTradePrice;
    }

    public void setCandleAccTradePrice(Double candleAccTradePrice) {
        this.candleAccTradePrice = candleAccTradePrice;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CoinCandle{" +
                "code='" + code + '\'' +
                ", openingPrice=" + openingPrice +
                ", highPrice=" + highPrice +
                ", lowPrice=" + lowPrice +
                ", tradePrice=" + tradePrice +
                ", candleAccTradePrice=" + candleAccTradePrice +
                ", timestamp=" + timestamp +
                '}';
    }
}
