package com.stocks.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
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
