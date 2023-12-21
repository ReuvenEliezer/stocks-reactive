package com.stock.services;

import com.stocks.entities.ExcelData;
import com.stocks.services.ExcelGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class StockTest {

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
    void excelGeneratorFullDataTest() throws IOException {
//        ExcelData excelData = ExcelData.builder()
//                .fileName("U.S.DividendChampions-2023-02-28-5519.xlsx")
//                .sheetName("All CCC")
//                .startRowIndex(6)
//                .symbolColumnIndex(1)
//                .isUpdateDivYield(false)
//                .isUpdatePrice(true)
//                .priceColumnIndex(8)
//                .divYieldColumnIndex(9)
//                .totalRows(714)
//                .build();
        ExcelData excelData = new ExcelData("U.S.DividendChampions-2023-03-31-5012.xlsx",
                "All CCC", 6, 1, false, true,
                8, 9, 714);
        ExcelGenerator excelGenerator = new ExcelGenerator(webClient, excelData);
        excelGenerator.update();
    }

    @Test
    void excelGeneratorShortDataTest() throws IOException {
        ExcelData excelData = new ExcelData("U.S.DividendChampions-LIVE.xlsx", "All", 3, 0, false, true,
                5, 6, 0);
        ExcelGenerator excelGenerator = new ExcelGenerator(webClient, excelData);
        excelGenerator.update();
    }

}
