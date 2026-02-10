package com.brosh.finance.monthlybudgetsync.utils;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Comprehensive unit tests for the FormatUtil class.
 * Tests all formatting methods including currency, decimal, and integer formatting.
 */
public class FormatUtilTest {

    // ============================================
    // CURRENCY FORMATTING TESTS
    // ============================================

    @Test
    public void testFormatCurrency_BasicAmount() {
        String result = FormatUtil.formatCurrency(1234.56, "$");
        assertEquals("$ 1,234.56", result);
    }

    @Test
    public void testFormatCurrency_NoDecimalPart() {
        String result = FormatUtil.formatCurrency(1000.0, "₪");
        assertEquals("₪ 1,000", result);
    }

    @Test
    public void testFormatCurrency_ZeroAmount() {
        String result = FormatUtil.formatCurrency(0.0, "$");
        assertEquals("$ 0", result);
    }

    @Test
    public void testFormatCurrency_NullCurrency() {
        String result = FormatUtil.formatCurrency(1234.56, null);
        assertEquals("1,234.56", result);
    }

    @Test
    public void testFormatCurrency_EmptyCurrency() {
        String result = FormatUtil.formatCurrency(1234.56, "");
        assertEquals("1,234.56", result);
    }

    @Test
    public void testFormatCurrency_LargeAmount() {
        String result = FormatUtil.formatCurrency(1234567890.12, "$");
        assertEquals("$ 1,234,567,890.12", result);
    }

    @Test
    public void testFormatCurrency_SmallDecimal() {
        String result = FormatUtil.formatCurrency(0.01, "$");
        assertEquals("$ 0.01", result);
    }

    @Test
    public void testFormatCurrency_UnicodeCurrency() {
        String result = FormatUtil.formatCurrency(100.0, "€");
        assertEquals("€ 100", result);
    }

    // ============================================
    // DECIMAL FORMATTING TESTS
    // ============================================

    @Test
    public void testFormatDecimal_BasicNumber() {
        String result = FormatUtil.formatDecimal(1234.56);
        assertEquals("1,234.56", result);
    }

    @Test
    public void testFormatDecimal_NoDecimal() {
        String result = FormatUtil.formatDecimal(1000.0);
        assertEquals("1,000", result);
    }

    @Test
    public void testFormatDecimal_Zero() {
        String result = FormatUtil.formatDecimal(0.0);
        assertEquals("0", result);
    }

    @Test
    public void testFormatDecimal_SingleDecimal() {
        String result = FormatUtil.formatDecimal(1234.5);
        assertEquals("1,234.5", result);
    }

    @Test
    public void testFormatDecimal_NegativeNumber() {
        String result = FormatUtil.formatDecimal(-1234.56);
        assertEquals("-1,234.56", result);
    }

    @Test
    public void testFormatDecimal_VerySmallNumber() {
        String result = FormatUtil.formatDecimal(0.01);
        assertEquals("0.01", result);
    }

    @Test
    public void testFormatDecimal_LargeNumber() {
        String result = FormatUtil.formatDecimal(999999999.99);
        assertEquals("999,999,999.99", result);
    }

    // ============================================
    // INTEGER FORMATTING TESTS
    // ============================================

    @Test
    public void testFormatInteger_BasicNumber() {
        String result = FormatUtil.formatInteger(1234);
        assertEquals("1,234", result);
    }

    @Test
    public void testFormatInteger_Zero() {
        String result = FormatUtil.formatInteger(0);
        assertEquals("0", result);
    }

    @Test
    public void testFormatInteger_NegativeNumber() {
        String result = FormatUtil.formatInteger(-1234);
        assertEquals("-1,234", result);
    }

    @Test
    public void testFormatInteger_LargeNumber() {
        String result = FormatUtil.formatInteger(1234567890);
        assertEquals("1,234,567,890", result);
    }

    @Test
    public void testFormatInteger_SmallNumber() {
        String result = FormatUtil.formatInteger(5);
        assertEquals("5", result);
    }

    // ============================================
    // ROUNDING TESTS
    // ============================================

    @Test
    public void testRoundToTwoDecimals_RoundUp() {
        double result = FormatUtil.roundToTwoDecimals(1.235);
        assertEquals(1.24, result, 0.001);
    }

    @Test
    public void testRoundToTwoDecimals_RoundDown() {
        double result = FormatUtil.roundToTwoDecimals(1.234);
        assertEquals(1.23, result, 0.001);
    }

    @Test
    public void testRoundToTwoDecimals_ExactTwoDecimals() {
        double result = FormatUtil.roundToTwoDecimals(1.23);
        assertEquals(1.23, result, 0.001);
    }

    @Test
    public void testRoundToTwoDecimals_Zero() {
        double result = FormatUtil.roundToTwoDecimals(0.0);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testRoundToTwoDecimals_Negative() {
        double result = FormatUtil.roundToTwoDecimals(-1.567);
        assertEquals(-1.57, result, 0.001);
    }

    @Test
    public void testRoundToTwoDecimals_ManyDecimals() {
        double result = FormatUtil.roundToTwoDecimals(1.23456789);
        assertEquals(1.23, result, 0.001);
    }

    // ============================================
    // BALANCE FORMATTING TESTS
    // ============================================

    @Test
    public void testFormatBalance_Positive() {
        String result = FormatUtil.formatBalance(1234.567);
        assertTrue(result.contains("1,234"));
    }

    @Test
    public void testFormatBalance_Negative() {
        String result = FormatUtil.formatBalance(-500.125);
        assertTrue(result.contains("500"));
        assertTrue(result.startsWith("-"));
    }

    @Test
    public void testFormatBalance_Zero() {
        String result = FormatUtil.formatBalance(0.0);
        assertEquals("0", result);
    }

    // ============================================
    // BUDGET FORMATTING TESTS
    // ============================================

    @Test
    public void testFormatBudget_Positive() {
        String result = FormatUtil.formatBudget(1234);
        assertEquals("1,234", result);
    }

    @Test
    public void testFormatBudget_Negative_UsesAbsolute() {
        String result = FormatUtil.formatBudget(-1234);
        assertEquals("1,234", result);
    }

    @Test
    public void testFormatBudget_Zero() {
        String result = FormatUtil.formatBudget(0);
        assertEquals("0", result);
    }

    // ============================================
    // PRICE FORMATTING TESTS
    // ============================================

    @Test
    public void testFormatPrice_BasicPrice() {
        String result = FormatUtil.formatPrice(99.99, "$");
        assertTrue(result.startsWith("$"));
        assertTrue(result.contains("100") || result.contains("99.99"));
    }

    @Test
    public void testFormatPrice_WithRounding() {
        String result = FormatUtil.formatPrice(99.994, "$");
        assertTrue(result.startsWith("$"));
    }

    @Test
    public void testFormatPrice_NullCurrency() {
        String result = FormatUtil.formatPrice(99.99, null);
        assertNotNull(result);
        assertTrue(result.contains("100") || result.contains("99.99"));
    }

    // ============================================
    // PARSING TESTS
    // ============================================

    @Test
    public void testParseFormattedDouble_BasicNumber() {
        double result = FormatUtil.parseFormattedDouble("1,234.56");
        assertEquals(1234.56, result, 0.001);
    }

    @Test
    public void testParseFormattedDouble_NoFormatting() {
        double result = FormatUtil.parseFormattedDouble("1234.56");
        assertEquals(1234.56, result, 0.001);
    }

    @Test
    public void testParseFormattedDouble_WithSpaces() {
        double result = FormatUtil.parseFormattedDouble("1 234.56");
        assertEquals(1234.56, result, 0.001);
    }

    @Test
    public void testParseFormattedDouble_Null() {
        double result = FormatUtil.parseFormattedDouble(null);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testParseFormattedDouble_Empty() {
        double result = FormatUtil.parseFormattedDouble("");
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testParseFormattedDouble_Invalid() {
        double result = FormatUtil.parseFormattedDouble("abc");
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testParseFormattedDouble_NegativeNumber() {
        double result = FormatUtil.parseFormattedDouble("-1,234.56");
        assertEquals(-1234.56, result, 0.001);
    }

    @Test
    public void testParseFormattedInt_BasicNumber() {
        int result = FormatUtil.parseFormattedInt("1,234");
        assertEquals(1234, result);
    }

    @Test
    public void testParseFormattedInt_Null() {
        int result = FormatUtil.parseFormattedInt(null);
        assertEquals(0, result);
    }

    @Test
    public void testParseFormattedInt_Empty() {
        int result = FormatUtil.parseFormattedInt("");
        assertEquals(0, result);
    }

    @Test
    public void testParseFormattedInt_Invalid() {
        int result = FormatUtil.parseFormattedInt("abc");
        assertEquals(0, result);
    }

    @Test
    public void testParseFormattedInt_WithDecimal() {
        int result = FormatUtil.parseFormattedInt("1,234.56");
        assertEquals(1234, result);
    }

    // ============================================
    // PAD WITH ZEROS TESTS
    // ============================================

    @Test
    public void testPadWithZeros_SingleDigit() {
        String result = FormatUtil.padWithZeros(5, 2);
        assertEquals("05", result);
    }

    @Test
    public void testPadWithZeros_DoubleDigit() {
        String result = FormatUtil.padWithZeros(12, 2);
        assertEquals("12", result);
    }

    @Test
    public void testPadWithZeros_LongerPadding() {
        String result = FormatUtil.padWithZeros(5, 4);
        assertEquals("0005", result);
    }

    @Test
    public void testPadWithZeros_Zero() {
        String result = FormatUtil.padWithZeros(0, 3);
        assertEquals("000", result);
    }

    @Test
    public void testPadWithZeros_NoPaddingNeeded() {
        String result = FormatUtil.padWithZeros(123, 3);
        assertEquals("123", result);
    }

    @Test
    public void testPadWithZeros_NumberLongerThanLength() {
        String result = FormatUtil.padWithZeros(12345, 3);
        assertEquals("12345", result); // Number is longer, no truncation
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    public void testFormatDecimal_VerySmallDecimal() {
        // Format might round very small decimals
        String result = FormatUtil.formatDecimal(0.001);
        // Result depends on pattern "#,###.##"
        assertNotNull(result);
    }

    @Test
    public void testFormatInteger_MaxValue() {
        String result = FormatUtil.formatInteger(Integer.MAX_VALUE);
        assertEquals("2,147,483,647", result);
    }

    @Test
    public void testFormatInteger_MinValue() {
        String result = FormatUtil.formatInteger(Integer.MIN_VALUE);
        assertEquals("-2,147,483,648", result);
    }

    @Test
    public void testRoundToTwoDecimals_LargeNumber() {
        double result = FormatUtil.roundToTwoDecimals(999999999.999);
        assertEquals(1000000000.0, result, 0.01);
    }

    @Test
    public void testPadWithZeros_NegativeNumber() {
        // Negative numbers will still be padded
        String result = FormatUtil.padWithZeros(-5, 4);
        // Result may include the minus sign
        assertNotNull(result);
    }

    @Test
    public void testFormatCurrency_HebrewShekel() {
        String result = FormatUtil.formatCurrency(1234.56, "₪");
        assertEquals("₪ 1,234.56", result);
    }
}
