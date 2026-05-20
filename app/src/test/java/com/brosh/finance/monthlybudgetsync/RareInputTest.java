package com.brosh.finance.monthlybudgetsync;

import static org.junit.Assert.*;

import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.utils.BudgetInputUtil;
import com.brosh.finance.monthlybudgetsync.utils.FormatUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.ValidationUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Exhaustive rare / malformed input tests for parsing, validation, and models.
 * Guards against NumberFormatException, NPE, and silent bad data on save.
 */
public class RareInputTest {

    // ============================================
    // BUDGET AMOUNT — rare strings
    // ============================================

    @Test
    public void parseBudgetAmount_rareInputsNeverThrow() {
        String[] inputs = {
                null, "", " ", "\t", "\n",
                "0", "00", "000",
                "-100", "+500",
                "12.5", "12,5", "1,234.56", "1.234,56",
                "999999999999", "2147483647", "2147483648",
                "abc", "12abc", "abc12", "12.34.56",
                ".", ",", "-", "+",
                "NaN", "Infinity", "1e10", "1.5E3",
                "１２３", // full-width digits
                "12\u00A0500", // non-breaking space
                "  1 2 3  ",
                "$100", "100₪", "100%",
                "null", "undefined",
                String.valueOf(Character.MAX_VALUE),
        };
        for (String input : inputs) {
            try {
                int result = BudgetInputUtil.parseBudgetAmount(input);
                assertTrue("Negative parsed amount should be clamped or zero: " + input, result >= 0);
            } catch (Exception e) {
                fail("parseBudgetAmount threw for input '" + input + "': " + e);
            }
        }
    }

    @Test
    public void parseBudgetAmount_specificEdgeCases() {
        assertEquals(0, BudgetInputUtil.parseBudgetAmount("-50"));
        assertEquals(0, BudgetInputUtil.parseBudgetAmount("abc"));
        assertEquals(13, BudgetInputUtil.parseBudgetAmount("12.6"));
        assertEquals(1500, BudgetInputUtil.parseBudgetAmount("1.5E3"));
        assertEquals(1234, BudgetInputUtil.parseBudgetAmount("1,234"));
    }

    // ============================================
    // CHARGE DAY — rare strings
    // ============================================

    @Test
    public void parseChargeDay_rareInputsNeverThrow() {
        String[] inputs = {
                null, "", " ", "-5", "0", "1", "31", "32", "99", "999",
                "abc", "15.5", "1,5", "１５",
                String.valueOf(Integer.MAX_VALUE),
        };
        for (String input : inputs) {
            try {
                int day = BudgetInputUtil.parseChargeDay(input);
                assertTrue("Day out of range for input: " + input, day >= 1 && day <= 31);
            } catch (Exception e) {
                fail("parseChargeDay threw for '" + input + "': " + e);
            }
        }
    }

    // ============================================
    // VALIDATION UTIL — rare numeric strings
    // ============================================

    @Test
    public void validationUtil_parseDouble_rareInputs() {
        String[] inputs = {
                null, "", " ", "abc", "-1.5", "0", "0.0",
                "1,234.56", "1.234,56", "12..34",
                "NaN", "Infinity", "1e308",
        };
        for (String input : inputs) {
            try {
                ValidationUtil.parseDouble(input, -999.0);
            } catch (Exception e) {
                fail("parseDouble threw for '" + input + "': " + e);
            }
        }
    }

    @Test
    public void validationUtil_parseInt_rareInputs() {
        String[] inputs = {
                null, "", "abc", "12.34", "1,234", "-99",
                String.valueOf(Long.MAX_VALUE),
        };
        for (String input : inputs) {
            try {
                ValidationUtil.parseInt(input, 0);
            } catch (Exception e) {
                fail("parseInt threw for '" + input + "': " + e);
            }
        }
    }

    @Test
    public void validationUtil_isValidPositiveNumber_rareInputs() {
        assertFalse(ValidationUtil.isValidPositiveNumber(null));
        assertFalse(ValidationUtil.isValidPositiveNumber(""));
        assertFalse(ValidationUtil.isValidPositiveNumber("0"));
        assertFalse(ValidationUtil.isValidPositiveNumber("-5"));
        assertFalse(ValidationUtil.isValidPositiveNumber("abc"));
        assertTrue(ValidationUtil.isValidPositiveNumber("0.001"));
        assertTrue(ValidationUtil.isValidPositiveNumber("1,234.56"));
    }

    // ============================================
    // BUDGET ROW VALIDATION — rare categories
    // ============================================

    @Test
    public void validateRow_unicodeAndLongNames() {
        List<String> cats = new ArrayList<>(Collections.singletonList("מזון"));
        assertEquals(BudgetInputUtil.RowError.NONE,
                BudgetInputUtil.validateRow("מזון", 100, false, null, cats));

        String longName = "A".repeat(500);
        cats.clear();
        cats.add(longName);
        assertEquals(BudgetInputUtil.RowError.NONE,
                BudgetInputUtil.validateRow(longName, 1, false, null, cats));
    }

    @Test
    public void validateRow_firebaseIllegalCharsInShop() {
        String sep = TextUtil.getSeparator();
        List<String> cats = new ArrayList<>(Collections.singletonList("Rent"));
        for (char illegal : new char[]{'.', '$', '#', '[', ']', '/'}) {
            String shop = "Store" + illegal + "X";
            cats.set(0, "Rent");
            assertEquals(BudgetInputUtil.RowError.ILLEGAL_SHOP_CHAR,
                    BudgetInputUtil.validateRow("Rent", 100, true, shop, cats));
        }
    }

    @Test
    public void validateRow_whitespaceOnlyCategory() {
        List<String> cats = new ArrayList<>(Collections.singletonList(""));
        assertEquals(BudgetInputUtil.RowError.EMPTY_CATEGORY,
                BudgetInputUtil.validateRow("   ", 100, false, null, cats));
    }

    // ============================================
    // BUILD BUDGET — extreme values
    // ============================================

    @Test
    public void buildBudget_extremeValuesClampedSafely() {
        Budget maxVal = BudgetInputUtil.buildBudget("X", Integer.MAX_VALUE, false, null, 99, 1);
        assertEquals(Integer.MAX_VALUE, maxVal.getValue());

        Budget negVal = BudgetInputUtil.buildBudget("X", -1000, false, null, 0, 1);
        assertEquals(0, negVal.getValue());
        assertEquals(1, negVal.getChargeDay()); // clamped 0 -> 1 in Budget constructor
    }

    // ============================================
    // BUDGET COMPARISON — rare lists
    // ============================================

    @Test
    public void budgetComparison_emptyLists() {
        List<Budget> empty = Collections.emptyList();
        assertFalse(BudgetInputUtil.hasBudgetChanges(empty, empty));
        assertFalse(BudgetInputUtil.hasOriginContentChanged(empty, empty));
        assertTrue(BudgetInputUtil.getAddedBudgets(empty, empty).isEmpty());
    }

    @Test
    public void budgetComparison_nullLikeCategories() {
        Budget a = new Budget(null, 100, false, null, 1, 1);
        Budget b = new Budget(null, 100, false, null, 1, 1);
        assertTrue(a.equals(b));
        assertFalse(BudgetInputUtil.hasBudgetChanges(
                Collections.singletonList(a),
                Collections.singletonList(b)));
    }

    // ============================================
    // FORMAT UTIL — rare formatting
    // ============================================

    @Test
    public void formatUtil_rareDoublesNeverThrow() {
        double[] values = {
                0, -0.0, Double.MIN_VALUE, Double.MAX_VALUE,
                0.1 + 0.2, 999999999999.99, -123.45,
        };
        for (double v : values) {
            try {
                String s = FormatUtil.formatDecimal(v);
                assertNotNull(s);
                FormatUtil.parseFormattedDouble(s);
            } catch (Exception e) {
                fail("FormatUtil failed for value " + v + ": " + e);
            }
        }
    }

    // ============================================
    // CATEGORY — rare withdrawal amounts
    // ============================================

    @Test
    public void category_rareWithdrawals() {
        Category cat = new Category("id", "Test", 100.0, 100);
        cat.withdrawal(0);
        assertEquals(100.0, cat.getBalance(), 0.001);
        cat.withdrawal(-50); // uses Math.abs — still subtracts 50
        assertEquals(50.0, cat.getBalance(), 0.001);
        cat.withdrawal(Double.MAX_VALUE);
        assertTrue(Double.isFinite(cat.getBalance()));
    }

    // ============================================
    // TEXT UTIL — rare strings
    // ============================================

    @Test
    public void textUtil_rareStringsNeverThrow() {
        String[] inputs = {null, "", " ", "\u200B", "a", "A", "hello world", "user@x.com"};
        for (String s : inputs) {
            try {
                TextUtil.getWordCapitalLetter(s);
                TextUtil.getEmailComma(s);
                TextUtil.safeTrim(s);
            } catch (Exception e) {
                fail("TextUtil failed for '" + s + "': " + e);
            }
        }
    }

    @Test
    public void textUtil_sentenceWithMultipleSeparators() {
        String result = TextUtil.getSentenceCapitalLetter("hello-world_test", '-');
        assertFalse(result.isEmpty());
    }

    // ============================================
    // PASSWORD / EMAIL edge cases (no Android Patterns)
    // ============================================

    @Test
    public void passwordValidation_rareInputs() {
        assertFalse(ValidationUtil.isValidPassword(null));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword("12345"));
        assertTrue(ValidationUtil.isValidPassword("      "));
        assertFalse(ValidationUtil.doPasswordsMatch(null, "x"));
        assertFalse(ValidationUtil.doPasswordsMatch("x", null));
    }

    @Test
    public void illegalFirebaseChars_comprehensive() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("a.b"));
        assertFalse(ValidationUtil.containsIllegalFirebaseChars(null));
        assertFalse(ValidationUtil.containsIllegalFirebaseChars("valid_name-123"));
    }

    // ============================================
    // LOCALE-SENSITIVE number strings (common user mistakes)
    // ============================================

    @Test
    public void parseBudgetAmount_localeStyleNumbers() {
        // US style with comma thousands
        assertEquals(1234567, BudgetInputUtil.parseBudgetAmount("1,234,567"));
        // Trailing/leading junk handled gracefully
        assertEquals(0, BudgetInputUtil.parseBudgetAmount("  "));
    }
}
