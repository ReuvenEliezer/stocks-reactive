package com.stock.services;

import com.stocks.entities.ExcelData;
import com.stocks.services.ExcelGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class StockTest {

    private static WebClient webClient;

    @BeforeAll
    static void init() {
        final int size = 16 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
        webClient = WebClient.builder()
//                .defaultHeaders()
                .exchangeStrategies(strategies)
                .build();
    }


    @Test
    public void excelGeneratorFullDataTest() throws IOException {
        ExcelData excelData = new ExcelData("U.S.DividendChampions-2022-11-30-8271 (2).xlsx", "All CCC", 6, 1, false, true,
                8, 9, 719);
        ExcelGenerator excelGenerator = new ExcelGenerator(webClient, excelData);
        excelGenerator.update();
    }

    @Test
    public void excelGeneratorShortDataTest() throws IOException {
        ExcelData excelData = new ExcelData("U.S.DividendChampions-LIVE.xlsx", "All", 3, 0, false, true,
                5, 6, 0);
        ExcelGenerator excelGenerator = new ExcelGenerator(webClient, excelData);
        excelGenerator.update();
    }

}
