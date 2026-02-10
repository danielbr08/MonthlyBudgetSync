package com.brosh.finance.monthlybudgetsync.utils;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Comprehensive unit tests for the ValidationUtil class.
 * Tests all validation methods including string, email, password, and numeric validation.
 */
public class ValidationUtilTest {

    // ============================================
    // STRING VALIDATION TESTS
    // ============================================

    @Test
    public void testIsEmpty_Null() {
        assertTrue(ValidationUtil.isEmpty(null));
    }

    @Test
    public void testIsEmpty_EmptyString() {
        assertTrue(ValidationUtil.isEmpty(""));
    }

    @Test
    public void testIsEmpty_WhitespaceOnly() {
        assertTrue(ValidationUtil.isEmpty("   "));
        assertTrue(ValidationUtil.isEmpty("\t\n"));
    }

    @Test
    public void testIsEmpty_ValidString() {
        assertFalse(ValidationUtil.isEmpty("hello"));
        assertFalse(ValidationUtil.isEmpty("  hello  "));
    }

    @Test
    public void testIsNotEmpty_Null() {
        assertFalse(ValidationUtil.isNotEmpty(null));
    }

    @Test
    public void testIsNotEmpty_EmptyString() {
        assertFalse(ValidationUtil.isNotEmpty(""));
    }

    @Test
    public void testIsNotEmpty_ValidString() {
        assertTrue(ValidationUtil.isNotEmpty("hello"));
    }

    @Test
    public void testSafeTrim_Null() {
        assertEquals("", ValidationUtil.safeTrim(null));
    }

    @Test
    public void testSafeTrim_EmptyString() {
        assertEquals("", ValidationUtil.safeTrim(""));
    }

    @Test
    public void testSafeTrim_WhitespaceOnly() {
        assertEquals("", ValidationUtil.safeTrim("   "));
    }

    @Test
    public void testSafeTrim_StringWithWhitespace() {
        assertEquals("hello", ValidationUtil.safeTrim("  hello  "));
    }

    @Test
    public void testSafeTrim_NoWhitespace() {
        assertEquals("hello", ValidationUtil.safeTrim("hello"));
    }

    // ============================================
    // EMAIL VALIDATION TESTS
    // Note: Email validation uses Android's Patterns class which is not
    // available in unit tests. These tests are moved to instrumented tests.
    // ============================================

    // Email validation tests moved to MonthlyBudgetSyncInstrumentedTest
    // as they require Android framework classes (android.util.Patterns)

    // ============================================
    // PASSWORD VALIDATION TESTS
    // ============================================

    @Test
    public void testIsValidPassword_ValidPassword() {
        assertTrue(ValidationUtil.isValidPassword("password123"));
        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertTrue(ValidationUtil.isValidPassword("a1b2c3"));
    }

    @Test
    public void testIsValidPassword_MinimumLength() {
        assertTrue(ValidationUtil.isValidPassword("123456")); // Exactly 6 chars
        assertFalse(ValidationUtil.isValidPassword("12345")); // 5 chars
    }

    @Test
    public void testIsValidPassword_Null() {
        assertFalse(ValidationUtil.isValidPassword(null));
    }

    @Test
    public void testIsValidPassword_EmptyString() {
        assertFalse(ValidationUtil.isValidPassword(""));
    }

    @Test
    public void testDoPasswordsMatch_Matching() {
        assertTrue(ValidationUtil.doPasswordsMatch("password123", "password123"));
    }

    @Test
    public void testDoPasswordsMatch_NotMatching() {
        assertFalse(ValidationUtil.doPasswordsMatch("password123", "different"));
    }

    @Test
    public void testDoPasswordsMatch_FirstNull() {
        assertFalse(ValidationUtil.doPasswordsMatch(null, "password"));
    }

    @Test
    public void testDoPasswordsMatch_SecondNull() {
        assertFalse(ValidationUtil.doPasswordsMatch("password", null));
    }

    @Test
    public void testDoPasswordsMatch_BothNull() {
        assertFalse(ValidationUtil.doPasswordsMatch(null, null));
    }

    @Test
    public void testDoPasswordsMatch_CaseSensitive() {
        assertFalse(ValidationUtil.doPasswordsMatch("Password", "password"));
    }

    // ============================================
    // NUMERIC VALIDATION TESTS
    // ============================================

    @Test
    public void testIsValidPositiveNumber_Valid() {
        assertTrue(ValidationUtil.isValidPositiveNumber("123"));
        assertTrue(ValidationUtil.isValidPositiveNumber("0.001"));
        assertTrue(ValidationUtil.isValidPositiveNumber("999999"));
        assertTrue(ValidationUtil.isValidPositiveNumber("1,234.56"));
    }

    @Test
    public void testIsValidPositiveNumber_Invalid() {
        assertFalse(ValidationUtil.isValidPositiveNumber("0"));
        assertFalse(ValidationUtil.isValidPositiveNumber("-5"));
        assertFalse(ValidationUtil.isValidPositiveNumber("abc"));
        assertFalse(ValidationUtil.isValidPositiveNumber(""));
    }

    @Test
    public void testIsValidPositiveNumber_Null() {
        assertFalse(ValidationUtil.isValidPositiveNumber(null));
    }

    @Test
    public void testIsValidNonNegativeNumber_Valid() {
        assertTrue(ValidationUtil.isValidNonNegativeNumber("0"));
        assertTrue(ValidationUtil.isValidNonNegativeNumber("123"));
        assertTrue(ValidationUtil.isValidNonNegativeNumber("0.5"));
        assertTrue(ValidationUtil.isValidNonNegativeNumber("1,000"));
    }

    @Test
    public void testIsValidNonNegativeNumber_Invalid() {
        assertFalse(ValidationUtil.isValidNonNegativeNumber("-5"));
        assertFalse(ValidationUtil.isValidNonNegativeNumber("-0.001"));
        assertFalse(ValidationUtil.isValidNonNegativeNumber("abc"));
    }

    @Test
    public void testIsValidNonNegativeNumber_Null() {
        assertFalse(ValidationUtil.isValidNonNegativeNumber(null));
    }

    @Test
    public void testParseDouble_Valid() {
        assertEquals(123.45, ValidationUtil.parseDouble("123.45", 0), 0.001);
        assertEquals(1234.56, ValidationUtil.parseDouble("1,234.56", 0), 0.001);
    }

    @Test
    public void testParseDouble_Invalid() {
        assertEquals(0.0, ValidationUtil.parseDouble("abc", 0), 0.001);
        assertEquals(-1.0, ValidationUtil.parseDouble("invalid", -1.0), 0.001);
    }

    @Test
    public void testParseDouble_Null() {
        assertEquals(0.0, ValidationUtil.parseDouble(null, 0), 0.001);
    }

    @Test
    public void testParseDouble_Empty() {
        assertEquals(0.0, ValidationUtil.parseDouble("", 0), 0.001);
    }

    @Test
    public void testParseInt_Valid() {
        assertEquals(123, ValidationUtil.parseInt("123", 0));
        assertEquals(1234, ValidationUtil.parseInt("1,234", 0));
    }

    @Test
    public void testParseInt_Invalid() {
        assertEquals(0, ValidationUtil.parseInt("abc", 0));
        assertEquals(-1, ValidationUtil.parseInt("invalid", -1));
    }

    @Test
    public void testParseInt_Null() {
        assertEquals(0, ValidationUtil.parseInt(null, 0));
    }

    @Test
    public void testParseInt_Decimal() {
        // Decimal numbers should fail to parse as int
        assertEquals(0, ValidationUtil.parseInt("123.45", 0));
    }

    // ============================================
    // SPECIAL CHARACTER VALIDATION TESTS
    // ============================================

    @Test
    public void testContainsIllegalFirebaseChars_WithDot() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test.value"));
    }

    @Test
    public void testContainsIllegalFirebaseChars_WithDollar() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test$value"));
    }

    @Test
    public void testContainsIllegalFirebaseChars_WithHash() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test#value"));
    }

    @Test
    public void testContainsIllegalFirebaseChars_WithBrackets() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test[value]"));
    }

    @Test
    public void testContainsIllegalFirebaseChars_WithSlash() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test/value"));
    }

    @Test
    public void testContainsIllegalFirebaseChars_ValidString() {
        assertFalse(ValidationUtil.containsIllegalFirebaseChars("valid-key_123"));
        assertFalse(ValidationUtil.containsIllegalFirebaseChars("normalText"));
    }

    @Test
    public void testContainsIllegalFirebaseChars_Null() {
        assertFalse(ValidationUtil.containsIllegalFirebaseChars(null));
    }

    @Test
    public void testContainsSeparator_Found() {
        assertTrue(ValidationUtil.containsSeparator("hello->world", "->"));
        assertTrue(ValidationUtil.containsSeparator("test,value", ","));
    }

    @Test
    public void testContainsSeparator_NotFound() {
        assertFalse(ValidationUtil.containsSeparator("hello world", "->"));
    }

    @Test
    public void testContainsSeparator_NullString() {
        assertFalse(ValidationUtil.containsSeparator(null, "->"));
    }

    // ============================================
    // CONSTANTS TESTS
    // ============================================

    @Test
    public void testMinPasswordLengthConstant() {
        assertEquals(6, ValidationUtil.MIN_PASSWORD_LENGTH);
    }

    @Test
    public void testMaxCategoryNameLengthConstant() {
        assertEquals(50, ValidationUtil.MAX_CATEGORY_NAME_LENGTH);
    }

    @Test
    public void testMaxShopNameLengthConstant() {
        assertEquals(50, ValidationUtil.MAX_SHOP_NAME_LENGTH);
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    public void testParseDouble_VeryLargeNumber() {
        double result = ValidationUtil.parseDouble("999999999999.99", 0);
        assertEquals(999999999999.99, result, 0.001);
    }

    @Test
    public void testParseDouble_ScientificNotation() {
        double result = ValidationUtil.parseDouble("1.5E10", 0);
        assertEquals(1.5E10, result, 0.001);
    }

    // Email validation tests require Android framework - see instrumented tests

    @Test
    public void testIsValidPassword_LongPassword() {
        String longPassword = "a".repeat(100);
        assertTrue(ValidationUtil.isValidPassword(longPassword));
    }

    @Test
    public void testIsValidPassword_WhitespacePassword() {
        assertTrue(ValidationUtil.isValidPassword("      ")); // 6 spaces is valid by length
    }

    @Test
    public void testContainsIllegalFirebaseChars_AllIllegalChars() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("."));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("$"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("#"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("["));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("]"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("/"));
    }

    @Test
    public void testParseDouble_NegativeNumber() {
        assertEquals(-123.45, ValidationUtil.parseDouble("-123.45", 0), 0.001);
    }

    @Test
    public void testParseInt_NegativeNumber() {
        assertEquals(-123, ValidationUtil.parseInt("-123", 0));
    }
}
