package com.stocks.entities;

import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Builder
public class ExcelData {

    private String fileName;
    private String sheetName;
    private int startRowIndex;

    private int symbolColumnIndex;

    private boolean isUpdateDivYield;

    private boolean isUpdatePrice;
    private int priceColumnIndex;
    private int divYieldColumnIndex;

    private int totalRows;



}
