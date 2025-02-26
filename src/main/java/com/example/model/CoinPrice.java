package com.example.model;


import java.io.Serializable;

public class CoinPrice implements Serializable {
    private String code;
    private Double price;
    private Double changeRate;

    // 기본 생성자
    public CoinPrice() {}

    // 모든 필드를 포함한 생성자
    public CoinPrice(String code, Double price, Double changeRate) {
        this.code = code;
        this.price = price;
        this.changeRate = changeRate;
    }

    // Getter와 Setter
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(Double changeRate) {
        this.changeRate = changeRate;
    }

    @Override
    public String toString() {
        return "CoinPrice{" +
                "code='" + code + '\'' +
                ", price=" + price +
                ", changeRate=" + changeRate +
                '}';
    }
}