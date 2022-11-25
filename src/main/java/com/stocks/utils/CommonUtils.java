package com.stocks.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CommonUtils {
    public static String formatException(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString();
        return String.format("Exception: %s %nMessage: %s %nStackTrace: %s", ex.getClass().getSimpleName(), ex.getMessage(), stackTrace);
    }
}
