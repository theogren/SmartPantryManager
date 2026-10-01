package com.example.myapplication;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Small helpers for displaying quantities and names nicely. */
public final class FormatUtil {

    private static final DecimalFormat QTY_FORMAT =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));

    private FormatUtil() { }

    /** 3.0 -> "3", 2.5 -> "2.5", 0.333 -> "0.33". */
    public static String qty(double value) {
        return QTY_FORMAT.format(value);
    }

    /** "olive oil" -> "Olive oil". */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }
}
