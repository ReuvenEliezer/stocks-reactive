package com.stocks.entities;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Stock {
    private String ticker;
    private String fullUri;

    private Double price;

    private Double divYield;

    private Double marketCap; //in trillion

    private Double peRatio;

    private Double epsRatio;


    public Stock(String ticker, String fullUri) {
        this.ticker = ticker;
        this.fullUri = fullUri;
    }
}
