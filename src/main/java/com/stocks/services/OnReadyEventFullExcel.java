//package com.stocks.services;
//
//import com.stocks.entities.Stock;
//import lombok.SneakyThrows;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.ss.usermodel.*;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.ApplicationListener;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StopWatch;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.function.Supplier;
//import java.util.stream.Stream;
//
//@Component
//public class OnReadyEventFullExcel implements ApplicationListener<ApplicationReadyEvent> {
//
//    private static final Logger logger = LoggerFactory.getLogger(OnReadyEventFullExcel.class);
//
//    private static final String uri = "https://finance.yahoo.com/quote/%s/";//https://seekingalpha.com/symbol/ATO/dividends/yield";
//    private static final int MAX_RETRY = 3;
//
//    private static final String stocksTickersFileName = "stocks_tickers.txt";
//
//    private static final String SHEET_NAME = "All CCC";
//    private static final String stocksTickersExcelFileName = "U.S.DividendChampions-2022-11-30-8271.xlsx";
//
//    private static final int symbolColumnIndex = 1;
//    private static final int startRowIndex = 6;
//
//    private static final int endOfStockTableInCell = 719;
//
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Autowired
//    private WebClient webClient;
//
//
//    @SneakyThrows
//    @Override
//    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start("reading excel file");
//        logger.info("onApplicationEvent");
////        List<String> stocksTickers = readFile();
//        List<String> stocksTickers = readExcelFile();
//        System.out.println(Arrays.asList(stocksTickers.toArray()));
//        stopWatch.stop();
//
//        stopWatch.start("mapping and prepare rest call");
//        Map<String, Mono<Stock>> stockTickerToDivValueMap = new HashMap<>();
//        for (String stockTicker : stocksTickers) {
////            if (stockTickerToDivValueMap.size() > 10) break;
//            Mono<Stock> stringMono = yahooFinance(stockTicker);
//            stockTickerToDivValueMap.put(stockTicker, stringMono);
//        }
//        stopWatch.stop();
//
//        Map<String, Stock> stockTickerToDataMap = new ConcurrentHashMap<>(stockTickerToDivValueMap.size());
//
//        stopWatch.start("block and fill data");
//        Flux.fromIterable(stockTickerToDivValueMap.values()).flatMap(Function.identity()).doOnNext(stock -> stockTickerToDataMap.put(stock.getTicker(), stock)).blockLast();
//
////        Flux.just(stockTickerToDivValueMap.values()).flatMap(Flux::fromIterable).blockLast();
////        Flux.concat(stockTickerToDivValueMap.values())
////                .doOnNext(stock -> {
////                    stockTickerToDataMap.put(stock.getTicker(), stock);
////                }).blockLast();
//
//        System.out.println(stockTickerToDataMap);
//        stopWatch.stop();
//
//        stopWatch.start("write to Csv file");
//        writeToCsv(stockTickerToDataMap);
//        stopWatch.stop();
//        stopWatch.start("write to Excel file");
//        writeToExcel(stockTickerToDataMap);
//        stopWatch.stop();
//
//        System.out.println(stopWatch.prettyPrint());
//
//    }
//
//    private void writeToCsv(Map<String, Stock> stockTickerToDataMap) {
//
//    }
//
//    private void writeToExcel(Map<String, Stock> stockTickerToDataMap) {
//        Workbook workbook = prepareExcelFile(stockTickerToDataMap);
//        try (FileOutputStream outputStream = new FileOutputStream(getStocksTickersPath(stocksTickersExcelFileName))) {
//            workbook.setForceFormulaRecalculation(true);
//            workbook.write(outputStream);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                workbook.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    private Workbook prepareExcelFile(Map<String, Stock> stockTickerToDataMap) {
//        try (FileInputStream inputStream = new FileInputStream(getStocksTickersPath(stocksTickersExcelFileName))) {
//            Workbook workbook = WorkbookFactory.create(inputStream);
//            Sheet sheet = workbook.getSheet(SHEET_NAME);
//            int priceColumnIndex = 8;
//            int divYieldColumnIndex = 9;
//            for (int rowIndex = startRowIndex; rowIndex <= endOfStockTableInCell; rowIndex++) {
//                Row row = sheet.getRow(rowIndex);
//
//                Cell symbol = row.getCell(symbolColumnIndex);
//                Stock stock = stockTickerToDataMap.get(symbol.getStringCellValue());
//
//                if (stock != null) {
//                    if (stock.getPrice() != null) {
//                        Cell price = row.createCell(priceColumnIndex);
//                        price.setCellValue(stock.getPrice());
//                    }
//
////                    if (stock.getDivYield() != null) {
////                        Cell divYieldColIndex = row.createCell(divYieldColumnIndex);
////                        divYieldColIndex.setCellValue(stock.getDivYield());
////                    }
//                }
//            }
//            return workbook;
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private List<String> readExcelFile() throws IOException {
//        List<String> stocksTickers = new ArrayList<>();
//        try (Workbook workbook = WorkbookFactory.create(new File(getStocksTickersPath(stocksTickersExcelFileName)))) {
//            Sheet sheet = workbook.getSheet(SHEET_NAME);
//            for (int rowIndex = startRowIndex; rowIndex <= endOfStockTableInCell; rowIndex++) {
//                Row row = sheet.getRow(rowIndex);
//                Cell cell = row.getCell(symbolColumnIndex);
//                if (cell != null && cell.getCellType().equals(CellType.STRING)) {
//                    // Found column and there is value in the cell.
//                    stocksTickers.add(cell.getStringCellValue());
//                    // Do something with the cellValueMaybeNull here ...
//                    // break; ???
//                }
//            }
//        }
//        return stocksTickers;
//    }
//
//    public static String getStocksTickersPath(String fileName) {
//        return "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + fileName;
//    }
//
//    private List<String> readFile() throws IOException {
//        try (Stream<String> lines = Files.lines(Paths.get(getStocksTickersPath(stocksTickersFileName)), StandardCharsets.UTF_8)) {
//            return lines.toList();
//        }
//    }
//
//    private Mono<Stock> yahooFinance(String stockTicker) {
//        String fullUri = String.format(uri, stockTicker);
////        ResponseEntity<String> data1 = restTemplate.getForEntity(fullUri, String.class);
////        extractData(data1.getBody(), stockTicker);
//        Stock stock = new Stock(stockTicker.toUpperCase(), fullUri);
//        return webClient.get().uri(fullUri).retrieve().bodyToMono(String.class).retry(MAX_RETRY).map(data -> extractData(data, stock)).doOnError(e -> {
//            logger.error("failed to retrieve on url {}", fullUri, e);
//        }).doOnRequest(t -> logger.info("doOnRequest to retrieve {}", fullUri)).onErrorReturn(stock);//                .flatMapMany(Flux::fromIterable)
//
//    }
//
//    private Stock extractData(String result, Stock stock) {
//        Document html = Jsoup.parse(result);
//        extractData(html, stock);
//        return stock;
//    }
//
//    private void extractPriceData(Document html, Stock stock) {
//        String priceAsText = html.body().getElementsByClass("Fw(b) Fz(36px) Mb(-4px) D(ib)").text();
//        stock.setPrice(Double.parseDouble(priceAsText));
//    }
//
//
//    private void extractData(Document html, Stock stock) {
//        extractPriceData(html, stock);
//        Elements elementsByClass = html.body().getElementsByClass("Ta(end) Fw(600) Lh(14px)");
//        String dividendYieldText = findElement(elementsByClass, "DIVIDEND_AND_YIELD-value").text();
//        String dividendYield = StringUtils.substringBetween(dividendYieldText, "(", "%");
//        stock.setDivYield(Double.parseDouble(dividendYield));
//        stock.setPeRatio(Double.parseDouble(findElement(elementsByClass, "PE_RATIO-value").text()));
//        stock.setEpsRatio(Double.parseDouble(findElement(elementsByClass, "EPS_RATIO-value").text()));
//        String marketCapValue = findElement(elementsByClass, "MARKET_CAP-value").text();
//        stock.setMarketCap(Double.parseDouble(StringUtils.substring(marketCapValue, 0, marketCapValue.length() - 1)));
//    }
//
//    private <T> void setValueIfExist(Supplier<T> getter, Consumer<T> setter) {
//        if (getter.get() != null) {
//            setter.accept(getter.get());
//        }
//    }
//
//    private Element findElement(Elements elements, String elementName) {
//        return elements.stream().filter(element -> element.attributes().get("data-test").equals(elementName)).findAny().orElseThrow();
//    }
//
//}
