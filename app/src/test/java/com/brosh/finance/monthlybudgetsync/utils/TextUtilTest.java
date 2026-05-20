package com.brosh.finance.monthlybudgetsync.utils;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for {@link TextUtil}.
 */
public class TextUtilTest {

    @Test
    public void getWordCapitalLetter_capitalizesFirstLetter() {
        assertEquals("Hello", TextUtil.getWordCapitalLetter("hello"));
        assertEquals("Hello", TextUtil.getWordCapitalLetter("Hello"));
    }

    @Test
    public void getWordCapitalLetter_nullOrEmpty() {
        assertEquals("", TextUtil.getWordCapitalLetter(null));
        assertEquals("", TextUtil.getWordCapitalLetter(""));
    }

    @Test
    public void getSentenceCapitalLetter_capitalizesWords() {
        assertEquals("Hello World", TextUtil.getSentenceCapitalLetter("hello world", ' '));
    }

    @Test
    public void getSeparator_notEmpty() {
        assertNotNull(TextUtil.getSeparator());
        assertFalse(TextUtil.getSeparator().isEmpty());
    }

    @Test
    public void getEmailComma_replacesDotsForFirebaseKeys() {
        assertEquals("user@example,com", TextUtil.getEmailComma("user@example.com"));
    }
}
