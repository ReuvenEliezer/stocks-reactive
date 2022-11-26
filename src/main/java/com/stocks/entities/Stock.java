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

    public Stock(String ticker, String fullUri) {
        this.ticker = ticker;
        this.fullUri = fullUri;
    }
}
