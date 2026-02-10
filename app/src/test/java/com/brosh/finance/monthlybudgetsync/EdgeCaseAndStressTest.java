package com.brosh.finance.monthlybudgetsync;

import static org.junit.Assert.*;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.*;
import com.brosh.finance.monthlybudgetsync.utils.*;

import org.junit.Test;

import java.util.*;

/**
 * Extended edge case and stress tests to catch potential production issues.
 * 
 * Tests cover:
 * - Currency precision (floating point issues with money)
 * - Concurrent modification scenarios
 * - Boundary conditions (max/min values)
 * - Data corruption scenarios
 * - Empty and null state handling
 * - Large dataset handling
 * - Input validation bypass attempts
 * - State consistency after operations
 */
public class EdgeCaseAndStressTest {

    // ============================================
    // CURRENCY PRECISION TESTS (Floating Point Issues)
    // ============================================

    @Test
    public void currencyPrecision_AddingSmallAmounts() {
        // Classic floating point issue: 0.1 + 0.2 != 0.3
        Category category = new Category("cat-1", "Test", 100.0, 100);
        
        // Simulate many small transactions
        for (int i = 0; i < 10; i++) {
            category.withdrawal(0.1);
        }
        
        // After 10 withdrawals of 0.1, balance should be 99.0
        // Due to floating point, it might be 98.99999999999999
        double expected = 99.0;
        double actual = category.getBalance();
        assertEquals("Currency precision issue after small transactions", expected, actual, 0.01);
    }

    @Test
    public void currencyPrecision_LargeNumbersWithDecimals() {
        Category category = new Category("cat-1", "Test", 1000000.99, 1000000);
        category.withdrawal(0.01);
        
        double expected = 1000000.98;
        assertEquals(expected, category.getBalance(), 0.001);
    }

    @Test
    public void currencyPrecision_SubtractingCents() {
        // Test: 10.00 - 0.30 should equal 9.70
        Category category = new Category("cat-1", "Test", 10.0, 10);
        category.withdrawal(0.30);
        
        assertEquals(9.70, category.getBalance(), 0.001);
    }

    @Test
    public void currencyPrecision_AccumulatingPennies() {
        Category category = new Category("cat-1", "Test", 100.0, 100);
        
        // Add 99 cents (0.99)
        for (int i = 0; i < 99; i++) {
            category.withdrawal(0.01);
        }
        
        assertEquals(99.01, category.getBalance(), 0.01);
    }

    @Test
    public void currencyPrecision_FormatAndParse() {
        double original = 1234.56;
        String formatted = FormatUtil.formatDecimal(original);
        double parsed = FormatUtil.parseFormattedDouble(formatted);
        
        assertEquals(original, parsed, 0.001);
    }

    // ============================================
    // CONCURRENT MODIFICATION SCENARIOS
    // ============================================

    @Test
    public void concurrentModification_CategoryTransactions() {
        Category category = new Category("cat-1", "Test", 100.0, 100);
        
        // Add some transactions
        for (int i = 0; i < 100; i++) {
            Transaction t = new Transaction("t" + i, i, "Test", "Cash", "Shop", null, 1.0);
            category.addTransactions("t" + i, t);
        }
        
        assertEquals(100, category.getTransactions().size());
        
        // Simulate iteration and modification (should not throw ConcurrentModificationException)
        Map<String, Transaction> copy = new HashMap<>(category.getTransactions());
        for (String key : copy.keySet()) {
            if (Integer.parseInt(key.substring(1)) % 2 == 0) {
                category.removeTransaction(key);
            }
        }
        
        assertEquals(50, category.getTransactions().size());
    }

    @Test
    public void concurrentModification_MonthCategories() {
        Month month = new Month("2024-06", 1L, 15);
        
        // Add categories
        for (int i = 0; i < 50; i++) {
            Category cat = new Category("cat-" + i, "Category " + i, 100.0, 100);
            month.addCategory("cat-" + i, cat);
        }
        
        assertEquals(50, month.getCategoryCount());
        
        // Safe iteration with copy
        Map<String, Category> copy = new HashMap<>(month.getCategories());
        for (String key : copy.keySet()) {
            month.updateSpecificCategory(key, new Category(key, "Updated", 200.0, 200));
        }
        
        assertEquals(50, month.getCategoryCount());
    }

    // ============================================
    // BOUNDARY CONDITIONS
    // ============================================

    @Test
    public void boundary_MaxIntegerBudget() {
        Budget budget = new Budget("Test", Integer.MAX_VALUE, false, null, 15, 1);
        assertEquals(Integer.MAX_VALUE, budget.getValue());
        
        Category category = new Category("cat-1", "Test", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, category.getBudget());
    }

    @Test
    public void boundary_MaxDoublePrice() {
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, trans.getPrice(), 0.001);
    }

    @Test
    public void boundary_MinValueHandling() {
        // Very small positive value
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, Double.MIN_VALUE);
        assertTrue(trans.getPrice() > 0);
    }

    @Test
    public void boundary_ZeroEverything() {
        Budget budget = new Budget("Test", 0, false, null, 1, 0);
        assertEquals(0, budget.getValue());
        
        Category category = new Category("cat-1", "Test", 0.0, 0);
        assertEquals(0.0, category.getBalance(), 0.001);
        assertEquals(0.0, category.getUsagePercentage(), 0.001);
    }

    @Test
    public void boundary_NegativeOverflow() {
        Category category = new Category("cat-1", "Test", 0.0, 100);
        
        // Withdraw more than balance, causing negative
        category.withdrawal(Double.MAX_VALUE);
        
        assertTrue(category.getBalance() < 0);
        assertTrue(category.isOverBudget());
    }

    @Test
    public void boundary_TransactionIdNumeratorOverflow() {
        Month month = new Month("2024-06", 1L, 15);
        month.setTranIdNumerator(Integer.MAX_VALUE - 1);
        
        int id1 = month.getNextTransactionId();
        int id2 = month.getNextTransactionId();
        
        assertEquals(Integer.MAX_VALUE - 1, id1);
        // This will overflow to negative
        assertTrue(id2 < 0 || id2 == Integer.MAX_VALUE);
    }

    @Test
    public void boundary_ChargeDayEdges() {
        // Day 1
        Budget b1 = new Budget("Test", 100, false, null, 1, 0);
        assertEquals(1, b1.getChargeDay());
        
        // Day 31
        Budget b31 = new Budget("Test", 100, false, null, 31, 0);
        assertEquals(31, b31.getChargeDay());
        
        // Day 0 -> clamped to 1
        Budget b0 = new Budget("Test", 100, false, null, 0, 0);
        assertEquals(1, b0.getChargeDay());
        
        // Day 32 -> clamped to 31
        Budget b32 = new Budget("Test", 100, false, null, 32, 0);
        assertEquals(31, b32.getChargeDay());
    }

    // ============================================
    // DATA CORRUPTION SCENARIOS
    // ============================================

    @Test
    public void dataCorruption_NullFieldsAfterDeserialization() {
        // Simulate Firebase deserialization with missing fields
        Budget budget = new Budget();
        budget.setCategoryName(null);
        budget.setShop(null);
        
        // Should not crash when accessing
        assertNull(budget.getCategoryName());
        assertNull(budget.getShop());
        assertNotNull(budget.toString());
    }

    @Test
    public void dataCorruption_EmptyCollections() {
        Month month = new Month();
        
        // Empty categories should not crash
        assertEquals(0, month.getCategoryCount());
        assertFalse(month.hasCategory("any"));
        assertNull(month.getCategory("any"));
    }

    // Date parsing with invalid format tests moved to instrumented tests
    // because they use android.util.Log which is not available in unit tests

    @Test
    public void dataCorruption_TransactionWithAllNulls() {
        Transaction trans = new Transaction();
        
        assertNull(trans.getId());
        assertNull(trans.getCategory());
        assertNull(trans.getPaymentMethod());
        assertNull(trans.getShop());
        assertNull(trans.getPayDate());
        assertNull(trans.getRegistrationDate());
        assertFalse(trans.isDeleted());
        
        // Should not crash
        assertNotNull(trans.toString());
        trans.formatDateFields(); // Should handle nulls
    }

    @Test
    public void dataCorruption_CategoryWithNullTransactionsMap() {
        Category category = new Category();
        category.setTransactions(null);
        
        // Should recover to empty map
        assertNotNull(category.getTransactions());
        assertEquals(0, category.getTransactions().size());
    }

    // ============================================
    // INPUT VALIDATION BYPASS ATTEMPTS
    // ============================================

    @Test
    public void inputValidation_SqlInjectionAttempt() {
        String maliciousInput = "'; DROP TABLE users; --";
        
        Budget budget = new Budget(maliciousInput, 100, false, maliciousInput, 15, 1);
        assertEquals(maliciousInput, budget.getCategoryName());
        assertEquals(maliciousInput, budget.getShop());
        
        // This input doesn't contain Firebase illegal chars (. $ # [ ] /)
        // but it could still be problematic - testing that data is stored as-is
        assertNotNull(budget.toString());
    }

    @Test
    public void inputValidation_XssAttempt() {
        String xssInput = "<script>alert('xss')</script>";
        
        Transaction trans = new Transaction("t1", 1, xssInput, "Cash", xssInput, null, 10.0);
        trans.setComment(xssInput);
        
        assertEquals(xssInput, trans.getCategory());
        assertEquals(xssInput, trans.getShop());
        assertEquals(xssInput, trans.getComment());
    }

    @Test
    public void inputValidation_PathTraversal() {
        String pathInput = "../../../etc/passwd";
        
        assertTrue(ValidationUtil.containsIllegalFirebaseChars(pathInput));
    }

    @Test
    public void inputValidation_VeryLongString() {
        String longString = "a".repeat(10000);
        
        Budget budget = new Budget(longString, 100, false, longString, 15, 1);
        assertEquals(longString, budget.getCategoryName());
        assertEquals(longString, budget.getShop());
        
        // Test formatting with long strings
        assertNotNull(budget.toString());
    }

    @Test
    public void inputValidation_SpecialUnicodeCharacters() {
        String unicodeInput = "Test\u0000Null\u200BZeroWidth\uFEFFBOM";
        
        Budget budget = new Budget(unicodeInput, 100, false, null, 15, 1);
        assertEquals(unicodeInput, budget.getCategoryName());
    }

    @Test
    public void inputValidation_NumericOverflow() {
        // Try to parse very large number
        double result = ValidationUtil.parseDouble("999999999999999999999999999999", 0);
        assertTrue(Double.isInfinite(result) || result == Double.MAX_VALUE || result > 0);
    }

    @Test
    public void inputValidation_NegativeNumbers() {
        // Negative budget should be clamped to 0
        Budget budget = new Budget("Test", -999999, false, null, 15, 1);
        assertEquals(0, budget.getValue());
        
        // Negative price should be clamped to 0
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, -999.99);
        assertEquals(0.0, trans.getPrice(), 0.001);
    }

    // ============================================
    // STATE CONSISTENCY TESTS
    // ============================================

    @Test
    public void stateConsistency_AfterMultipleOperations() {
        Category category = new Category("cat-1", "Test", 1000.0, 1000);
        
        // Series of operations
        category.withdrawal(100);
        category.deposit(50);
        category.withdrawal(25);
        
        Transaction t1 = new Transaction("t1", 1, "Test", "Cash", "Shop", null, 75.0);
        category.addTransactions("t1", t1);
        
        assertEquals(925.0, category.getBalance(), 0.001);
        assertEquals(1, category.getTransactions().size());
        assertEquals(75.0, category.getTotalSpent(), 0.001);
    }

    @Test
    public void stateConsistency_DeleteRestoreTransaction() {
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, 100.0);
        
        // Initial state
        assertTrue(trans.isActive());
        assertFalse(trans.isDeleted());
        
        // Delete
        trans.markDeleted();
        assertFalse(trans.isActive());
        assertTrue(trans.isDeleted());
        
        // Restore
        trans.restore();
        assertTrue(trans.isActive());
        assertFalse(trans.isDeleted());
        
        // Multiple deletes
        trans.markDeleted();
        trans.markDeleted();
        trans.markDeleted();
        assertTrue(trans.isDeleted());
        
        // Single restore brings back
        trans.restore();
        assertTrue(trans.isActive());
    }

    @Test
    public void stateConsistency_MonthActiveStatus() {
        // Current month should be active
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int monthNum = cal.get(Calendar.MONTH) + 1;
        String yearMonth = year + "-" + (monthNum < 10 ? "0" + monthNum : monthNum);
        
        Month month = new Month(yearMonth, 1L, 15);
        assertTrue(month.isActive());
        
        // Force inactive
        month.setActive(false);
        assertFalse(month.isActive());
        
        // Refresh should restore to correct state
        month.setIsActive();
        assertTrue(month.isActive());
    }

    @Test
    public void stateConsistency_UserOwnership() {
        User user = new User("uid-1", "Test", "test@test.com", null, null);
        
        // Initially owner (dbKey defaults to uid)
        assertTrue(user.isOwner());
        
        // Change to guest
        user.setDbKey("other-uid");
        assertFalse(user.isOwner());
        
        // Change back to owner
        user.setDbKey("uid-1");
        assertTrue(user.isOwner());
    }

    // ============================================
    // LARGE DATASET HANDLING
    // ============================================

    @Test
    public void largeDataset_ManyTransactions() {
        Category category = new Category("cat-1", "Test", 1000000.0, 1000000);
        
        // Add 1000 transactions
        for (int i = 0; i < 1000; i++) {
            Transaction t = new Transaction("t" + i, i, "Test", "Cash", "Shop " + i, null, 10.0);
            category.addTransactions("t" + i, t);
        }
        
        assertEquals(1000, category.getTransactions().size());
        
        // Verify retrieval
        assertNotNull(category.getTransaction("t500"));
        assertEquals(500, category.getTransaction("t500").getIdPerMonth());
    }

    @Test
    public void largeDataset_ManyCategories() {
        Month month = new Month("2024-06", 1L, 15);
        
        // Add 100 categories
        for (int i = 0; i < 100; i++) {
            Category cat = new Category("cat-" + i, "Category " + i, 1000.0, 1000);
            month.addCategory("cat-" + i, cat);
        }
        
        assertEquals(100, month.getCategoryCount());
        
        // Verify retrieval
        assertNotNull(month.getCategory("cat-50"));
        assertEquals("Category 50", month.getCategory("cat-50").getName());
    }

    @Test
    public void largeDataset_SortingPerformance() {
        List<Transaction> transactions = new ArrayList<>();
        
        // Create 500 transactions with random data
        Random random = new Random(42); // Fixed seed for reproducibility
        for (int i = 0; i < 500; i++) {
            Transaction t = new Transaction(
                "t" + i, 
                random.nextInt(1000),
                "Category" + (i % 10),
                "Method" + (i % 5),
                "Shop" + (i % 20),
                null,
                random.nextDouble() * 1000
            );
            transactions.add(t);
        }
        
        // Sort by various criteria
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_ID, Config.UP_ARROW);
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_PRICE, Config.DOWN_ARROW);
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_CATEGORY, Config.UP_ARROW);
        
        // Verify sorting completed
        assertEquals(500, transactions.size());
    }

    // ============================================
    // DATE EDGE CASES
    // ============================================

    @Test
    public void dateEdge_EndOfMonth() {
        // Test setting day 31 in February
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.FEBRUARY, 1);
        Date febDate = cal.getTime();
        
        Date result = DateUtil.setDayToDate(febDate, 31);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        // Should be clamped to Feb 29 (2024 is leap year)
        assertEquals(29, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void dateEdge_LeapYear() {
        // Feb 29 in leap year (2024)
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.FEBRUARY, 29);
        Date leapDate = cal.getTime();
        
        String yearMonth = DateUtil.getYearMonth(leapDate, "-");
        assertEquals("2024-02", yearMonth);
    }

    @Test
    public void dateEdge_NonLeapYear() {
        // Try to set Feb 29 in non-leap year (2023)
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.FEBRUARY, 1);
        Date febDate = cal.getTime();
        
        Date result = DateUtil.setDayToDate(febDate, 29);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        // Should be clamped to Feb 28
        assertEquals(28, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void dateEdge_YearChange() {
        // Dec to Jan transition
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.DECEMBER, 15);
        Date decDate = cal.getTime();
        
        Date nextMonth = DateUtil.getNextRefMonth(decDate);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(nextMonth);
        
        assertEquals(2025, resultCal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, resultCal.get(Calendar.MONTH));
    }

    @Test
    public void dateEdge_DistantPast() {
        Month month = new Month("1900-01", 1L, 15);
        assertFalse(month.isActive());
    }

    @Test
    public void dateEdge_DistantFuture() {
        Month month = new Month("2100-12", 1L, 15);
        assertFalse(month.isActive());
    }

    // ============================================
    // EQUALITY AND HASHING EDGE CASES
    // ============================================

    @Test
    public void equality_DifferentObjectsSameId() {
        Transaction t1 = new Transaction("same-id", 1, "Cat1", "Method1", "Shop1", null, 100.0);
        Transaction t2 = new Transaction("same-id", 2, "Cat2", "Method2", "Shop2", null, 200.0);
        
        // Should be equal because ID is the same
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void equality_NullVsEmpty() {
        User user1 = new User("uid", "Name", null, null, null);
        user1.setEmail(null);
        
        User user2 = new User("uid", "Name", "", null, null);
        user2.setEmail("");
        
        // Both have same UID, should be equal
        assertEquals(user1, user2);
    }

    @Test
    public void equality_InHashSet() {
        Set<Transaction> set = new HashSet<>();
        
        Transaction t1 = new Transaction("id-1", 1, "Cat", "Method", "Shop", null, 100.0);
        Transaction t2 = new Transaction("id-1", 2, "DiffCat", "DiffMethod", "DiffShop", null, 200.0);
        Transaction t3 = new Transaction("id-2", 3, "Cat", "Method", "Shop", null, 100.0);
        
        set.add(t1);
        set.add(t2); // Should not add (same ID)
        set.add(t3);
        
        assertEquals(2, set.size());
    }

    @Test
    public void equality_InHashMap() {
        Map<Category, String> map = new HashMap<>();
        
        Category c1 = new Category("cat-1", "Name1", 100.0, 100);
        Category c2 = new Category("cat-1", "Name2", 200.0, 200);
        
        map.put(c1, "first");
        map.put(c2, "second"); // Should overwrite
        
        assertEquals(1, map.size());
        assertEquals("second", map.get(c1));
    }

    // ============================================
    // CLONE AND COPY EDGE CASES
    // ============================================

    @Test
    public void clone_CategoryDeepCopy() throws CloneNotSupportedException {
        Category original = new Category("cat-1", "Test", 100.0, 100);
        Transaction t = new Transaction("t1", 1, "Test", "Cash", "Shop", null, 10.0);
        original.addTransactions("t1", t);
        
        Category cloned = (Category) original.clone();
        
        // Modify original
        original.setBalance(50.0);
        original.addTransactions("t2", new Transaction("t2", 2, "Test", "Cash", "Shop", null, 20.0));
        
        // Clone should be independent
        assertEquals(100.0, cloned.getBalance(), 0.001);
        assertEquals(1, cloned.getTransactions().size());
    }

    // ============================================
    // SERIALIZATION EDGE CASES
    // ============================================

    @Test
    public void serialization_AllObjectsSerializable() {
        // Verify all objects implement Serializable correctly
        assertTrue(java.io.Serializable.class.isAssignableFrom(Budget.class));
        assertTrue(java.io.Serializable.class.isAssignableFrom(Category.class));
        assertTrue(java.io.Serializable.class.isAssignableFrom(Transaction.class));
        assertTrue(java.io.Serializable.class.isAssignableFrom(Month.class));
        assertTrue(java.io.Serializable.class.isAssignableFrom(User.class));
    }

    // ============================================
    // COMPUTED PROPERTY EDGE CASES
    // ============================================

    @Test
    public void computed_UsagePercentageExtremes() {
        // 0% usage
        Category cat1 = new Category("cat-1", "Test", 1000.0, 1000);
        assertEquals(0.0, cat1.getUsagePercentage(), 0.001);
        
        // 100% usage
        Category cat2 = new Category("cat-2", "Test", 0.0, 1000);
        assertEquals(100.0, cat2.getUsagePercentage(), 0.001);
        
        // 200% usage (over budget)
        Category cat3 = new Category("cat-3", "Test", -1000.0, 1000);
        assertEquals(200.0, cat3.getUsagePercentage(), 0.001);
        
        // Zero budget (division by zero protection)
        Category cat4 = new Category("cat-4", "Test", 0.0, 0);
        assertEquals(0.0, cat4.getUsagePercentage(), 0.001);
    }

    @Test
    public void computed_TotalSpent() {
        Category category = new Category("cat-1", "Test", 750.0, 1000);
        assertEquals(250.0, category.getTotalSpent(), 0.001);
        
        // Over budget
        category.setBalance(-100.0);
        assertEquals(1100.0, category.getTotalSpent(), 0.001);
    }
}
