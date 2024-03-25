package com.stocks.controllers;

import com.stocks.entities.ExcelData;
import com.stocks.services.ExcelGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;


@RestController
@RequestMapping("/v1")
public class ExcelStocksController {

    private final WebClient webClient;

    public ExcelStocksController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping(value = "/update-data")
    public void updateData(ExcelData excelData) throws IOException {
//        ExcelData excelData;
//        if (useShortExcel) {
//            excelData = new ExcelData("U.S.DividendChampions-LIVE.xlsx", "All", 3, 0, true,
//                    true, 5, 6, 0);
//        } else {
//            excelData = new ExcelData("U.S.DividendChampions-LIVE.xlsx", "All", 6, 1, false,
//                    true, 8, 9, 719);
//        }
        ExcelGenerator excelGenerator = new ExcelGenerator(webClient, excelData);
        excelGenerator.update();
    }
}
