package com.stocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication()
@ComponentScan(basePackages = {
        "com.stocks.config",
        "com.stocks.controllers",
        "com.stocks.services"
})
public class StockApp {
    public static void main(String[] args) {
        ReactorDebugAgent.init();
        SpringApplication.run(StockApp.class, args);
    }
}