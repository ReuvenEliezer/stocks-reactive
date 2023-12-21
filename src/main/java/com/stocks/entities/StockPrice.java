package com.stocks.entities;

import java.time.LocalDateTime;

public record StockPrice(String symbol, Double price, LocalDateTime dateTime) {
}
