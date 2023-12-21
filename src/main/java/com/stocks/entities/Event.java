package com.stocks.entities;

import java.time.LocalDateTime;

public record Event(long id, String type, LocalDateTime dateTime) {
}
