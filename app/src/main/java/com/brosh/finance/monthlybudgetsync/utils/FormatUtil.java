package com.brosh.finance.monthlybudgetsync.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for formatting values (currency, decimals, etc.).
 * Uses cached formatters for better performance.
 * Thread-safe with ThreadLocal formatters.
 */
public final class FormatUtil {
    
    private FormatUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    /**
     * Thread-local DecimalFormat for currency/number formatting.
     * Pattern: #,###.## (e.g., 1,234.56)
     */
    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT = ThreadLocal.withInitial(
        () -> new DecimalFormat("#,###.##")
    );
    
    /**
     * Thread-local DecimalFormat for integer formatting.
     * Pattern: #,### (e.g., 1,234)
     */
    private static final ThreadLocal<DecimalFormat> INTEGER_FORMAT = ThreadLocal.withInitial(
        () -> new DecimalFormat("#,###")
    );
    
    /**
     * Formats a double value with currency symbol.
     * Example: formatCurrency(1234.56, "$") returns "$ 1,234.56"
     *
     * @param value The value to format
     * @param currency The currency symbol
     * @return Formatted string with currency
     */
    @NonNull
    public static String formatCurrency(double value, @Nullable String currency) {
        String formattedValue = formatDecimal(value);
        if (currency == null || currency.isEmpty()) {
            return formattedValue;
        }
        return currency + " " + formattedValue;
    }
    
    /**
     * Formats a double value with two decimal places and thousand separators.
     * Example: formatDecimal(1234.567) returns "1,234.57"
     *
     * @param value The value to format
     * @return Formatted string
     */
    @NonNull
    public static String formatDecimal(double value) {
        return DECIMAL_FORMAT.get().format(value);
    }
    
    /**
     * Formats an integer value with thousand separators.
     * Example: formatInteger(1234) returns "1,234"
     *
     * @param value The value to format
     * @return Formatted string
     */
    @NonNull
    public static String formatInteger(int value) {
        return INTEGER_FORMAT.get().format(value);
    }
    
    /**
     * Rounds a double to two decimal places.
     * Example: roundToTwoDecimals(1234.5678) returns 1234.57
     *
     * @param value The value to round
     * @return Rounded value
     */
    public static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
    
    /**
     * Formats a balance value, adding color indication based on sign.
     * Positive/zero values use the regular format, negative values could be styled differently.
     *
     * @param value The balance value
     * @return Formatted balance string
     */
    @NonNull
    public static String formatBalance(double value) {
        double rounded = roundToTwoDecimals(value);
        return formatDecimal(rounded);
    }
    
    /**
     * Formats a budget value (always positive integer).
     *
     * @param value The budget value
     * @return Formatted budget string
     */
    @NonNull
    public static String formatBudget(int value) {
        return formatInteger(Math.abs(value));
    }
    
    /**
     * Formats a price value with currency symbol.
     *
     * @param price The price value
     * @param currency The currency symbol
     * @return Formatted price string
     */
    @NonNull
    public static String formatPrice(double price, @Nullable String currency) {
        double rounded = roundToTwoDecimals(price);
        return formatCurrency(rounded, currency);
    }
    
    /**
     * Parses a formatted number string back to double.
     * Handles thousand separators.
     *
     * @param formattedValue The formatted string
     * @return Parsed double value, or 0 if parsing fails
     */
    public static double parseFormattedDouble(@Nullable String formattedValue) {
        if (formattedValue == null || formattedValue.isEmpty()) {
            return 0;
        }
        
        try {
            // Remove thousand separators and parse
            String cleaned = formattedValue.replaceAll("[,\\s]", "");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Parses a formatted number string back to integer.
     * Handles thousand separators.
     *
     * @param formattedValue The formatted string
     * @return Parsed integer value, or 0 if parsing fails
     */
    public static int parseFormattedInt(@Nullable String formattedValue) {
        return (int) parseFormattedDouble(formattedValue);
    }
    
    /**
     * Pads a number with leading zeros.
     * Example: padWithZeros(5, 2) returns "05"
     *
     * @param value The value to pad
     * @param length The desired length
     * @return Padded string
     */
    @NonNull
    public static String padWithZeros(int value, int length) {
        return String.format(Locale.US, "%0" + length + "d", value);
    }
}
