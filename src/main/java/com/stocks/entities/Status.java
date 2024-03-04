package com.stocks.entities;

public enum Status {
    CHUNK_COMPLETED("chunk-completed"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String name;
    Status(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}