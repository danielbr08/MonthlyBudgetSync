package com.brosh.finance.monthlybudgetsync.utils;

import static org.junit.Assert.*;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Comprehensive unit tests for the ComparatorUtil class.
 * Tests all sorting comparators and sort methods.
 */
public class ComparatorUtilTest {

    private List<Transaction> transactions;
    private List<Budget> budgets;

    @Before
    public void setUp() {
        transactions = new ArrayList<>();
        budgets = new ArrayList<>();
        
        // Create test transactions
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2024, Calendar.JANUARY, 10);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.JANUARY, 5);
        
        Calendar cal3 = Calendar.getInstance();
        cal3.set(2024, Calendar.JANUARY, 15);
        
        Transaction t1 = new Transaction("t1", 3, "Groceries", "Cash", "Walmart", cal1.getTime(), 50.0);
        Transaction t2 = new Transaction("t2", 1, "Entertainment", "Card", "Amazon", cal2.getTime(), 100.0);
        Transaction t3 = new Transaction("t3", 2, "Food", "Cash", "Target", cal3.getTime(), 25.0);
        
        transactions.add(t1);
        transactions.add(t2);
        transactions.add(t3);
        
        // Create test budgets
        Budget b1 = new Budget("Groceries", 500, false, null, 15, 2);
        Budget b2 = new Budget("Entertainment", 200, false, null, 1, 1);
        Budget b3 = new Budget("Utilities", 300, true, "Electric Co", 10, 3);
        
        budgets.add(b1);
        budgets.add(b2);
        budgets.add(b3);
    }

    // ============================================
    // SORT BY ID TESTS
    // ============================================

    @Test
    public void testSort_ById_Ascending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_ID, Config.UP_ARROW);
        
        assertEquals(1, transactions.get(0).getIdPerMonth());
        assertEquals(2, transactions.get(1).getIdPerMonth());
        assertEquals(3, transactions.get(2).getIdPerMonth());
    }

    @Test
    public void testSort_ById_Descending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_ID, Config.DOWN_ARROW);
        
        assertEquals(3, transactions.get(0).getIdPerMonth());
        assertEquals(2, transactions.get(1).getIdPerMonth());
        assertEquals(1, transactions.get(2).getIdPerMonth());
    }

    // ============================================
    // SORT BY CATEGORY TESTS
    // ============================================

    @Test
    public void testSort_ByCategory_Ascending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CATEGORY, Config.UP_ARROW);
        
        assertEquals("Entertainment", transactions.get(0).getCategory());
        assertEquals("Food", transactions.get(1).getCategory());
        assertEquals("Groceries", transactions.get(2).getCategory());
    }

    @Test
    public void testSort_ByCategory_Descending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CATEGORY, Config.DOWN_ARROW);
        
        assertEquals("Groceries", transactions.get(0).getCategory());
        assertEquals("Food", transactions.get(1).getCategory());
        assertEquals("Entertainment", transactions.get(2).getCategory());
    }

    @Test
    public void testSort_ByCategory_CaseInsensitive() {
        transactions.clear();
        Transaction t1 = new Transaction("t1", 1, "apple", "Cash", "Shop", null, 10.0);
        Transaction t2 = new Transaction("t2", 2, "Banana", "Cash", "Shop", null, 10.0);
        Transaction t3 = new Transaction("t3", 3, "CHERRY", "Cash", "Shop", null, 10.0);
        
        transactions.add(t2);
        transactions.add(t3);
        transactions.add(t1);
        
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CATEGORY, Config.UP_ARROW);
        
        assertEquals("apple", transactions.get(0).getCategory());
        assertEquals("Banana", transactions.get(1).getCategory());
        assertEquals("CHERRY", transactions.get(2).getCategory());
    }

    @Test
    public void testSort_ByCategory_WithNulls() {
        transactions.get(1).setCategory(null);
        
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CATEGORY, Config.UP_ARROW);
        
        // Null should be at the end
        assertNull(transactions.get(2).getCategory());
    }

    // ============================================
    // SORT BY PAYMENT METHOD TESTS
    // ============================================

    @Test
    public void testSort_ByPaymentMethod_Ascending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_PAYMENT_METHOD, Config.UP_ARROW);
        
        assertEquals("Card", transactions.get(0).getPaymentMethod());
        assertEquals("Cash", transactions.get(1).getPaymentMethod());
        assertEquals("Cash", transactions.get(2).getPaymentMethod());
    }

    @Test
    public void testSort_ByPaymentMethod_Descending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_PAYMENT_METHOD, Config.DOWN_ARROW);
        
        assertEquals("Cash", transactions.get(0).getPaymentMethod());
        assertEquals("Cash", transactions.get(1).getPaymentMethod());
        assertEquals("Card", transactions.get(2).getPaymentMethod());
    }

    // ============================================
    // SORT BY STORE TESTS
    // ============================================

    @Test
    public void testSort_ByStore_Ascending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_STORE, Config.UP_ARROW);
        
        assertEquals("Amazon", transactions.get(0).getShop());
        assertEquals("Target", transactions.get(1).getShop());
        assertEquals("Walmart", transactions.get(2).getShop());
    }

    @Test
    public void testSort_ByStore_Descending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_STORE, Config.DOWN_ARROW);
        
        assertEquals("Walmart", transactions.get(0).getShop());
        assertEquals("Target", transactions.get(1).getShop());
        assertEquals("Amazon", transactions.get(2).getShop());
    }

    // ============================================
    // SORT BY PRICE TESTS
    // ============================================

    @Test
    public void testSort_ByPrice_Ascending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_PRICE, Config.UP_ARROW);
        
        assertEquals(25.0, transactions.get(0).getPrice(), 0.001);
        assertEquals(50.0, transactions.get(1).getPrice(), 0.001);
        assertEquals(100.0, transactions.get(2).getPrice(), 0.001);
    }

    @Test
    public void testSort_ByPrice_Descending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_PRICE, Config.DOWN_ARROW);
        
        assertEquals(100.0, transactions.get(0).getPrice(), 0.001);
        assertEquals(50.0, transactions.get(1).getPrice(), 0.001);
        assertEquals(25.0, transactions.get(2).getPrice(), 0.001);
    }

    // ============================================
    // SORT BY CHARGE DATE TESTS
    // ============================================

    @Test
    public void testSort_ByChargeDate_Ascending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CHARGE_DATE, Config.UP_ARROW);
        
        // Jan 5, Jan 10, Jan 15
        assertEquals("t2", transactions.get(0).getId());
        assertEquals("t1", transactions.get(1).getId());
        assertEquals("t3", transactions.get(2).getId());
    }

    @Test
    public void testSort_ByChargeDate_Descending() {
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CHARGE_DATE, Config.DOWN_ARROW);
        
        // Jan 15, Jan 10, Jan 5
        assertEquals("t3", transactions.get(0).getId());
        assertEquals("t1", transactions.get(1).getId());
        assertEquals("t2", transactions.get(2).getId());
    }

    @Test
    public void testSort_ByChargeDate_WithNulls() {
        transactions.get(0).setPayDate(null);
        
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CHARGE_DATE, Config.UP_ARROW);
        
        // Null should be at the end
        assertNull(transactions.get(2).getPayDate());
    }

    // ============================================
    // SORT BY REGISTRATION DATE TESTS
    // ============================================

    @Test
    public void testSort_ByRegistrationDate_Ascending() {
        // Set different registration dates
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2024, Calendar.JANUARY, 1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.JANUARY, 3);
        
        Calendar cal3 = Calendar.getInstance();
        cal3.set(2024, Calendar.JANUARY, 2);
        
        transactions.get(0).setRegistrationDate(cal1.getTime());
        transactions.get(1).setRegistrationDate(cal2.getTime());
        transactions.get(2).setRegistrationDate(cal3.getTime());
        
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_REGISTRATION_DATE, Config.UP_ARROW);
        
        // Check order: Jan 1, Jan 2, Jan 3
        assertEquals("t1", transactions.get(0).getId());
        assertEquals("t3", transactions.get(1).getId());
        assertEquals("t2", transactions.get(2).getId());
    }

    // ============================================
    // SORT BUDGETS TESTS
    // ============================================

    @Test
    public void testSortBudgets_Ascending() {
        ComparatorUtil.sortBudgets(budgets, true);
        
        assertEquals(1, budgets.get(0).getCatPriority());
        assertEquals(2, budgets.get(1).getCatPriority());
        assertEquals(3, budgets.get(2).getCatPriority());
    }

    @Test
    public void testSortBudgets_Descending() {
        ComparatorUtil.sortBudgets(budgets, false);
        
        assertEquals(3, budgets.get(0).getCatPriority());
        assertEquals(2, budgets.get(1).getCatPriority());
        assertEquals(1, budgets.get(2).getCatPriority());
    }

    // ============================================
    // NULL AND EMPTY LIST TESTS
    // ============================================

    @Test
    public void testSort_NullList() {
        // Should not throw
        ComparatorUtil.sort(null, Definitions.SORT_BY_ID, Config.UP_ARROW);
    }

    @Test
    public void testSort_EmptyList() {
        List<Transaction> emptyList = new ArrayList<>();
        ComparatorUtil.sort(emptyList, Definitions.SORT_BY_ID, Config.UP_ARROW);
        assertTrue(emptyList.isEmpty());
    }

    @Test
    public void testSortBudgets_NullList() {
        // Should not throw
        ComparatorUtil.sortBudgets(null, true);
    }

    @Test
    public void testSortBudgets_EmptyList() {
        List<Budget> emptyList = new ArrayList<>();
        ComparatorUtil.sortBudgets(emptyList, true);
        assertTrue(emptyList.isEmpty());
    }

    // ============================================
    // COMPARATOR DIRECT TESTS
    // ============================================

    @Test
    public void testCompareById_DirectComparator() {
        int result = ComparatorUtil.COMPARE_BY_ID.compare(transactions.get(0), transactions.get(1));
        // t1 has id 3, t2 has id 1
        assertTrue(result > 0);
    }

    @Test
    public void testCompareByPrice_DirectComparator() {
        int result = ComparatorUtil.COMPARE_BY_PRICE.compare(transactions.get(0), transactions.get(1));
        // t1 has price 50, t2 has price 100
        assertTrue(result < 0);
    }

    @Test
    public void testCompareByCategory_DirectComparator() {
        int result = ComparatorUtil.COMPARE_BY_CATEGORY.compare(transactions.get(0), transactions.get(1));
        // "Groceries" vs "Entertainment"
        assertTrue(result > 0); // G comes after E
    }

    @Test
    public void testCompareByCategoryPriority_DirectComparator() {
        int result = ComparatorUtil.COMPARE_BY_CATEGORY_PRIORITY.compare(budgets.get(0), budgets.get(1));
        // Priority 2 vs Priority 1
        assertTrue(result > 0);
    }

    @Test
    public void testCompareString_DirectComparator() {
        int result = ComparatorUtil.COMPARE_STRING.compare("apple", "Banana");
        // Case insensitive: a comes before b
        assertTrue(result < 0);
    }

    @Test
    public void testCompareString_WithNull() {
        int result = ComparatorUtil.COMPARE_STRING.compare("apple", null);
        // Non-null should come before null
        assertTrue(result < 0);
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    public void testSort_SingleElement() {
        List<Transaction> singleList = new ArrayList<>();
        singleList.add(transactions.get(0));
        
        ComparatorUtil.sort(singleList, Definitions.SORT_BY_ID, Config.UP_ARROW);
        
        assertEquals(1, singleList.size());
        assertEquals("t1", singleList.get(0).getId());
    }

    @Test
    public void testSort_DuplicateValues() {
        // Add transaction with same price as t1
        Transaction t4 = new Transaction("t4", 4, "Duplicate", "Cash", "Store", null, 50.0);
        transactions.add(t4);
        
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_PRICE, Config.UP_ARROW);
        
        // Should handle duplicates without error
        assertEquals(4, transactions.size());
    }

    @Test
    public void testSort_AllNullValues() {
        transactions.get(0).setCategory(null);
        transactions.get(1).setCategory(null);
        transactions.get(2).setCategory(null);
        
        // Should not throw
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CATEGORY, Config.UP_ARROW);
    }

    @Test
    public void testSort_InvalidSortType_DefaultsToId() {
        ComparatorUtil.sort(transactions, 999, Config.UP_ARROW);
        
        // Should default to sort by ID
        assertEquals(1, transactions.get(0).getIdPerMonth());
        assertEquals(2, transactions.get(1).getIdPerMonth());
        assertEquals(3, transactions.get(2).getIdPerMonth());
    }

    @Test
    public void testSort_MixedNullAndValidDates() {
        transactions.get(0).setPayDate(null);
        // t1 has null, t2 has Jan 5, t3 has Jan 15
        
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CHARGE_DATE, Config.UP_ARROW);
        
        // Valid dates first, null at end
        assertNotNull(transactions.get(0).getPayDate());
        assertNotNull(transactions.get(1).getPayDate());
        assertNull(transactions.get(2).getPayDate());
    }

    @Test
    public void testSortBudgets_SamePriority() {
        Budget b4 = new Budget("Same Priority", 100, false, null, 1, 2);
        budgets.add(b4);
        
        ComparatorUtil.sortBudgets(budgets, true);
        
        // Should handle same priorities
        assertEquals(4, budgets.size());
    }
}
