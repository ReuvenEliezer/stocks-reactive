package com.stocks.entities;

import java.time.Duration;

public record LoanDetails(double amount, double rate, Duration duration) {
}
