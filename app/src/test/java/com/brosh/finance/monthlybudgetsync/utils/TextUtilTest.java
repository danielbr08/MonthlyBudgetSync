package com.brosh.finance.monthlybudgetsync.utils;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for TextUtil class.
 * Note: Some methods like showMessage require Android context and are tested 
 * in instrumented tests.
 */
public class TextUtilTest {

    // ============================================
    // GET WORD CAPITAL LETTER TESTS
    // ============================================

    @Test
    public void testGetWordCapitalLetter_LowercaseFirst() {
        assertEquals("Hello", TextUtil.getWordCapitalLetter("hello"));
    }

    @Test
    public void testGetWordCapitalLetter_UppercaseFirst() {
        assertEquals("Hello", TextUtil.getWordCapitalLetter("Hello"));
    }

    @Test
    public void testGetWordCapitalLetter_SingleLetter() {
        assertEquals("A", TextUtil.getWordCapitalLetter("a"));
        assertEquals("A", TextUtil.getWordCapitalLetter("A"));
    }

    @Test
    public void testGetWordCapitalLetter_EmptyString() {
        assertEquals("", TextUtil.getWordCapitalLetter(""));
    }

    @Test
    public void testGetWordCapitalLetter_Null() {
        assertEquals("", TextUtil.getWordCapitalLetter(null));
    }

    @Test
    public void testGetWordCapitalLetter_NumberFirst() {
        assertEquals("123abc", TextUtil.getWordCapitalLetter("123abc"));
    }

    @Test
    public void testGetWordCapitalLetter_AllUppercase() {
        assertEquals("HELLO", TextUtil.getWordCapitalLetter("HELLO"));
    }

    // ============================================
    // GET SENTENCE CAPITAL LETTER TESTS
    // ============================================

    @Test
    public void testGetSentenceCapitalLetter_TwoWords() {
        assertEquals("Hello World", TextUtil.getSentenceCapitalLetter("hello world", ' '));
    }

    @Test
    public void testGetSentenceCapitalLetter_MultipleWords() {
        assertEquals("Hello Beautiful World", TextUtil.getSentenceCapitalLetter("hello beautiful world", ' '));
    }

    @Test
    public void testGetSentenceCapitalLetter_SingleWord() {
        assertEquals("Hello", TextUtil.getSentenceCapitalLetter("hello", ' '));
    }

    @Test
    public void testGetSentenceCapitalLetter_EmptyString() {
        assertEquals("", TextUtil.getSentenceCapitalLetter("", ' '));
    }

    @Test
    public void testGetSentenceCapitalLetter_Null() {
        assertEquals("", TextUtil.getSentenceCapitalLetter(null, ' '));
    }

    @Test
    public void testGetSentenceCapitalLetter_DifferentSeparator() {
        assertEquals("Hello-World", TextUtil.getSentenceCapitalLetter("hello-world", '-'));
    }

    @Test
    public void testGetSentenceCapitalLetter_MixedCase() {
        // Function capitalizes first letter but doesn't lowercase rest
        String result = TextUtil.getSentenceCapitalLetter("HELLO WORLD", ' ');
        assertTrue(result.startsWith("H"));
    }

    // ============================================
    // GET SEPARATOR TESTS
    // ============================================

    @Test
    public void testGetSeparator() {
        String separator = TextUtil.getSeparator();
        assertNotNull(separator);
        assertEquals("->", separator);
    }

    // ============================================
    // GET EMAIL COMMA TESTS
    // ============================================

    @Test
    public void testGetEmailComma_ValidEmail() {
        assertEquals("test@example,com", TextUtil.getEmailComma("test@example.com"));
    }

    @Test
    public void testGetEmailComma_MultipleDots() {
        assertEquals("user,name@mail,example,com", TextUtil.getEmailComma("user.name@mail.example.com"));
    }

    @Test
    public void testGetEmailComma_NoDots() {
        assertEquals("test@examplecom", TextUtil.getEmailComma("test@examplecom"));
    }

    @Test
    public void testGetEmailComma_Null() {
        assertEquals("", TextUtil.getEmailComma(null));
    }

    @Test
    public void testGetEmailComma_EmptyString() {
        assertEquals("", TextUtil.getEmailComma(""));
    }

    @Test
    public void testGetEmailComma_WithWhitespace() {
        assertEquals("test@example,com", TextUtil.getEmailComma("  test@example.com  "));
    }

    // ============================================
    // IS EMPTY TESTS
    // ============================================

    @Test
    public void testIsEmpty_Null() {
        assertTrue(TextUtil.isEmpty(null));
    }

    @Test
    public void testIsEmpty_EmptyString() {
        assertTrue(TextUtil.isEmpty(""));
    }

    @Test
    public void testIsEmpty_WhitespaceOnly() {
        assertTrue(TextUtil.isEmpty("   "));
        assertTrue(TextUtil.isEmpty("\t\n"));
    }

    @Test
    public void testIsEmpty_ValidString() {
        assertFalse(TextUtil.isEmpty("hello"));
        assertFalse(TextUtil.isEmpty("  hello  "));
    }

    // ============================================
    // SAFE TRIM TESTS
    // ============================================

    @Test
    public void testSafeTrim_ValidString() {
        assertEquals("hello", TextUtil.safeTrim("  hello  "));
    }

    @Test
    public void testSafeTrim_Null() {
        assertEquals("", TextUtil.safeTrim(null));
    }

    @Test
    public void testSafeTrim_EmptyString() {
        assertEquals("", TextUtil.safeTrim(""));
    }

    @Test
    public void testSafeTrim_NoTrimNeeded() {
        assertEquals("hello", TextUtil.safeTrim("hello"));
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    public void testUnicodeText() {
        // Hebrew text - first char doesn't need capitalization
        assertEquals("שלום", TextUtil.getWordCapitalLetter("שלום"));
        // Russian text - function only handles ASCII letters a-z
        String result = TextUtil.getWordCapitalLetter("привет");
        assertNotNull(result);
    }

    @Test
    public void testSpecialCharacters() {
        assertEquals("Hello!", TextUtil.getWordCapitalLetter("hello!"));
        assertEquals("@hello", TextUtil.getWordCapitalLetter("@hello"));
    }

    @Test
    public void testEmailComma_OnlyDots() {
        assertEquals(",,,", TextUtil.getEmailComma("..."));
    }

    @Test
    public void testSentenceCapitalLetter_ConsecutiveSeparators() {
        String result = TextUtil.getSentenceCapitalLetter("hello  world", ' ');
        assertNotNull(result);
    }
}
