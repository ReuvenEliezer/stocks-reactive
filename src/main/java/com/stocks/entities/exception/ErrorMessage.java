package com.stocks.entities.exception;

import java.time.LocalDateTime;

public class ErrorMessage {
    private int statusCode;
    private LocalDateTime dateTime;
    private String message;
    private String description;

    public ErrorMessage(int statusCode, LocalDateTime dateTime, String message, String description) {
        this.statusCode = statusCode;
        this.dateTime = dateTime;
        this.message = message;
        this.description = description;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
