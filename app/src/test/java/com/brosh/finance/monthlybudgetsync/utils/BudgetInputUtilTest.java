package com.brosh.finance.monthlybudgetsync.utils;

import static org.junit.Assert.*;

import com.brosh.finance.monthlybudgetsync.objects.Budget;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link BudgetInputUtil} — budget row parsing, validation, and comparison.
 */
public class BudgetInputUtilTest {

    // ============================================
    // PARSING
    // ============================================

    @Test
    public void parseBudgetAmount_emptyReturnsZero() {
        assertEquals(0, BudgetInputUtil.parseBudgetAmount(null));
        assertEquals(0, BudgetInputUtil.parseBudgetAmount(""));
        assertEquals(0, BudgetInputUtil.parseBudgetAmount("   "));
    }

    @Test
    public void parseBudgetAmount_integer() {
        assertEquals(500, BudgetInputUtil.parseBudgetAmount("500"));
        assertEquals(1234, BudgetInputUtil.parseBudgetAmount("1,234"));
    }

    @Test
    public void parseBudgetAmount_decimalRounded() {
        assertEquals(13, BudgetInputUtil.parseBudgetAmount("12.6"));
        assertEquals(12, BudgetInputUtil.parseBudgetAmount("12.4"));
    }

    @Test
    public void parseBudgetAmount_invalidReturnsZero() {
        assertEquals(0, BudgetInputUtil.parseBudgetAmount("abc"));
    }

    @Test
    public void parseBudgetAmount_negativeClampedToZero() {
        assertEquals(0, BudgetInputUtil.parseBudgetAmount("-50"));
    }

    @Test
    public void parseBudgetAmount_scientificNotation() {
        assertEquals(1500, BudgetInputUtil.parseBudgetAmount("1.5E3"));
    }

    @Test
    public void validateRow_whitespaceCategoryIsEmpty() {
        List<String> cats = new ArrayList<>(Collections.singletonList(""));
        assertEquals(BudgetInputUtil.RowError.EMPTY_CATEGORY,
                BudgetInputUtil.validateRow("   ", 100, false, null, cats));
    }

    @Test
    public void parseChargeDay_defaultsAndClamps() {
        assertEquals(1, BudgetInputUtil.parseChargeDay(null));
        assertEquals(1, BudgetInputUtil.parseChargeDay(""));
        assertEquals(15, BudgetInputUtil.parseChargeDay("15"));
        assertEquals(31, BudgetInputUtil.parseChargeDay("99"));
        assertEquals(1, BudgetInputUtil.parseChargeDay("0"));
    }

    // ============================================
    // ROW VALIDATION
    // ============================================

    @Test
    public void validateRow_valid() {
        List<String> categories = new ArrayList<>(Collections.singletonList("Food"));
        BudgetInputUtil.RowError error = BudgetInputUtil.validateRow(
                "Food", 100, false, null, categories);
        assertEquals(BudgetInputUtil.RowError.NONE, error);
    }

    @Test
    public void validateRow_emptyCategory() {
        List<String> categories = new ArrayList<>(Collections.singletonList(""));
        assertEquals(BudgetInputUtil.RowError.EMPTY_CATEGORY,
                BudgetInputUtil.validateRow("", 100, false, null, categories));
    }

    @Test
    public void validateRow_duplicateCategory() {
        List<String> categories = new ArrayList<>(Arrays.asList("Food", "Food"));
        assertEquals(BudgetInputUtil.RowError.DUPLICATE_CATEGORY,
                BudgetInputUtil.validateRow("Food", 100, false, null, categories));
    }

    @Test
    public void validateRow_zeroValue() {
        List<String> categories = new ArrayList<>(Collections.singletonList("Food"));
        assertEquals(BudgetInputUtil.RowError.ZERO_VALUE,
                BudgetInputUtil.validateRow("Food", 0, false, null, categories));
    }

    @Test
    public void validateRow_constPaymentRequiresShop() {
        List<String> categories = new ArrayList<>(Collections.singletonList("Rent"));
        assertEquals(BudgetInputUtil.RowError.EMPTY_SHOP,
                BudgetInputUtil.validateRow("Rent", 1000, true, "", categories));
    }

    @Test
    public void validateRow_constPaymentWithShop() {
        List<String> categories = new ArrayList<>(Collections.singletonList("Rent"));
        assertEquals(BudgetInputUtil.RowError.NONE,
                BudgetInputUtil.validateRow("Rent", 1000, true, "Landlord", categories));
    }

    @Test
    public void validateRow_illegalCategorySeparator() {
        List<String> categories = new ArrayList<>(Collections.singletonList("Bad" + TextUtil.getSeparator() + "Name"));
        assertEquals(BudgetInputUtil.RowError.ILLEGAL_CATEGORY_CHAR,
                BudgetInputUtil.validateRow("Bad" + TextUtil.getSeparator() + "Name", 10, false, null, categories));
    }

    // ============================================
    // BUILD BUDGET
    // ============================================

    @Test
    public void buildBudget_nonConstPaymentClearsShop() {
        Budget budget = BudgetInputUtil.buildBudget("Food", 50, false, "ignored", 5, 1);
        assertEquals("Food", budget.getCategoryName());
        assertEquals(50, budget.getValue());
        assertFalse(budget.isConstPayment());
        assertNull(budget.getShop());
    }

    @Test
    public void buildBudget_constPaymentKeepsShop() {
        Budget budget = BudgetInputUtil.buildBudget("Rent", 1000, true, "  Store  ", 15, 2);
        assertTrue(budget.isConstPayment());
        assertEquals("Store", budget.getShop());
        assertEquals(2, budget.getCatPriority());
    }

    // ============================================
    // COMPARISON
    // ============================================

    @Test
    public void hasBudgetChanges_sameList() {
        Budget a = new Budget("Food", 100, false, null, 1, 1);
        List<Budget> list = Collections.singletonList(a);
        assertFalse(BudgetInputUtil.hasBudgetChanges(list, list));
    }

    @Test
    public void hasBudgetChanges_addedCategory() {
        Budget a = new Budget("Food", 100, false, null, 1, 1);
        Budget b = new Budget("Transport", 50, false, null, 1, 2);
        List<Budget> original = Collections.singletonList(a);
        List<Budget> current = Arrays.asList(a, b);
        assertTrue(BudgetInputUtil.hasBudgetChanges(original, current));
    }

    @Test
    public void hasBudgetChanges_modifiedValue() {
        Budget oldB = new Budget("Food", 100, false, null, 1, 1);
        Budget newB = new Budget("Food", 200, false, null, 1, 1);
        assertTrue(BudgetInputUtil.hasBudgetChanges(
                Collections.singletonList(oldB),
                Collections.singletonList(newB)));
    }

    @Test
    public void hasOriginContentChanged_onlyAddition() {
        Budget a = new Budget("Food", 100, false, null, 1, 1);
        Budget b = new Budget("Transport", 50, false, null, 1, 2);
        List<Budget> original = Collections.singletonList(a);
        List<Budget> current = Arrays.asList(a, b);
        assertFalse(BudgetInputUtil.hasOriginContentChanged(original, current));
    }

    @Test
    public void hasOriginContentChanged_removedCategory() {
        Budget a = new Budget("Food", 100, false, null, 1, 1);
        Budget b = new Budget("Transport", 50, false, null, 1, 2);
        List<Budget> original = Arrays.asList(a, b);
        List<Budget> current = Collections.singletonList(a);
        assertTrue(BudgetInputUtil.hasOriginContentChanged(original, current));
    }

    @Test
    public void getAddedBudgets_returnsOnlyNew() {
        Budget a = new Budget("Food", 100, false, null, 1, 1);
        Budget b = new Budget("Transport", 50, false, null, 1, 2);
        List<Budget> added = BudgetInputUtil.getAddedBudgets(
                Collections.singletonList(a),
                Arrays.asList(a, b));
        assertEquals(1, added.size());
        assertEquals("Transport", added.get(0).getCategoryName());
    }
}
