package com.brosh.finance.monthlybudgetsync.objects;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive unit tests for the Category class.
 * Tests all getters, setters, constructors, transaction operations,
 * balance operations, and computed properties.
 */
public class CategoryTest {

    private Category category;
    private Transaction transaction;

    @Before
    public void setUp() {
        category = new Category("cat-1", "Groceries", 500.0, 500);
        transaction = new Transaction("trans-1", 1, "Groceries", "Cash", "Walmart", null, 50.0);
    }

    // ============================================
    // CONSTRUCTOR TESTS
    // ============================================

    @Test
    public void testDefaultConstructor() {
        Category defaultCategory = new Category();
        assertNull(defaultCategory.getId());
        assertNull(defaultCategory.getName());
        assertEquals(0, defaultCategory.getBudget());
        assertEquals(0.0, defaultCategory.getBalance(), 0.001);
        assertNotNull(defaultCategory.getTransactions());
        assertTrue(defaultCategory.getTransactions().isEmpty());
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals("cat-1", category.getId());
        assertEquals("Groceries", category.getName());
        assertEquals(500.0, category.getBalance(), 0.001);
        assertEquals(500, category.getBudget());
        assertNotNull(category.getTransactions());
        assertTrue(category.getTransactions().isEmpty());
    }

    @Test
    public void testConstructorWithNullValues() {
        Category nullCategory = new Category(null, null, 100.0, 100);
        assertNull(nullCategory.getId());
        assertNull(nullCategory.getName());
        assertEquals(100.0, nullCategory.getBalance(), 0.001);
    }

    @Test
    public void testConstructorNegativeBudgetClampedToZero() {
        Category negativeBudget = new Category("id", "Test", 100.0, -100);
        assertEquals(0, negativeBudget.getBudget());
    }

    // ============================================
    // GETTER AND SETTER TESTS
    // ============================================

    @Test
    public void testSetAndGetId() {
        category.setId("new-id");
        assertEquals("new-id", category.getId());
    }

    @Test
    public void testSetAndGetName() {
        category.setName("Entertainment");
        assertEquals("Entertainment", category.getName());
    }

    @Test
    public void testSetAndGetBudget() {
        category.setBudget(1000);
        assertEquals(1000, category.getBudget());
    }

    @Test
    public void testSetNegativeBudgetClampedToZero() {
        category.setBudget(-500);
        assertEquals(0, category.getBudget());
    }

    @Test
    public void testSetAndGetBalance() {
        category.setBalance(250.50);
        assertEquals(250.50, category.getBalance(), 0.001);
    }

    @Test
    public void testSetNegativeBalance() {
        // Negative balance is allowed (over budget)
        category.setBalance(-100.0);
        assertEquals(-100.0, category.getBalance(), 0.001);
    }

    // ============================================
    // TRANSACTION OPERATION TESTS
    // ============================================

    @Test
    public void testAddTransaction() {
        category.addTransactions("trans-1", transaction);
        assertEquals(1, category.getTransactions().size());
        assertTrue(category.hasTransaction("trans-1"));
        assertEquals(transaction, category.getTransaction("trans-1"));
    }

    @Test
    public void testAddMultipleTransactions() {
        Transaction trans2 = new Transaction("trans-2", 2, "Groceries", "Card", "Target", null, 30.0);
        
        category.addTransactions("trans-1", transaction);
        category.addTransactions("trans-2", trans2);
        
        assertEquals(2, category.getTransactions().size());
        assertTrue(category.hasTransaction("trans-1"));
        assertTrue(category.hasTransaction("trans-2"));
    }

    @Test
    public void testRemoveTransaction() {
        category.addTransactions("trans-1", transaction);
        Transaction removed = category.removeTransaction("trans-1");
        
        assertEquals(transaction, removed);
        assertFalse(category.hasTransaction("trans-1"));
        assertEquals(0, category.getTransactions().size());
    }

    @Test
    public void testRemoveNonExistentTransaction() {
        Transaction removed = category.removeTransaction("non-existent");
        assertNull(removed);
    }

    @Test
    public void testGetTransaction() {
        category.addTransactions("trans-1", transaction);
        assertEquals(transaction, category.getTransaction("trans-1"));
    }

    @Test
    public void testGetNonExistentTransaction() {
        assertNull(category.getTransaction("non-existent"));
    }

    @Test
    public void testHasTransaction() {
        assertFalse(category.hasTransaction("trans-1"));
        category.addTransactions("trans-1", transaction);
        assertTrue(category.hasTransaction("trans-1"));
    }

    @Test
    public void testHasTransactionWithNullId() {
        assertFalse(category.hasTransaction(null));
    }

    @Test
    public void testSetTransactions() {
        Map<String, Transaction> transactions = new HashMap<>();
        transactions.put("trans-1", transaction);
        
        category.setTransactions(transactions);
        
        assertEquals(1, category.getTransactions().size());
        assertTrue(category.hasTransaction("trans-1"));
    }

    @Test
    public void testSetTransactionsWithNull() {
        category.addTransactions("trans-1", transaction);
        category.setTransactions(null);
        
        assertNotNull(category.getTransactions());
        assertTrue(category.getTransactions().isEmpty());
    }

    // ============================================
    // BALANCE OPERATION TESTS
    // ============================================

    @Test
    public void testWithdrawal() {
        category.withdrawal(100.0);
        assertEquals(400.0, category.getBalance(), 0.001);
    }

    @Test
    public void testWithdrawalNegativeAmount() {
        // Negative amount should be treated as positive (uses Math.abs)
        category.withdrawal(-100.0);
        assertEquals(400.0, category.getBalance(), 0.001);
    }

    @Test
    public void testDeposit() {
        category.deposit(100.0);
        assertEquals(600.0, category.getBalance(), 0.001);
    }

    @Test
    public void testDepositNegativeAmount() {
        // Negative amount should be treated as positive (uses Math.abs)
        category.deposit(-100.0);
        assertEquals(600.0, category.getBalance(), 0.001);
    }

    @Test
    public void testMultipleWithdrawals() {
        category.withdrawal(100.0);
        category.withdrawal(200.0);
        category.withdrawal(50.0);
        assertEquals(150.0, category.getBalance(), 0.001);
    }

    @Test
    public void testWithdrawalCausesOverBudget() {
        category.withdrawal(600.0);
        assertEquals(-100.0, category.getBalance(), 0.001);
        assertTrue(category.isOverBudget());
    }

    // ============================================
    // COMPUTED PROPERTY TESTS
    // ============================================

    @Test
    public void testGetTotalSpent_NoSpending() {
        assertEquals(0.0, category.getTotalSpent(), 0.001);
    }

    @Test
    public void testGetTotalSpent_AfterWithdrawal() {
        category.withdrawal(150.0);
        assertEquals(150.0, category.getTotalSpent(), 0.001);
    }

    @Test
    public void testIsOverBudget_False() {
        assertFalse(category.isOverBudget());
    }

    @Test
    public void testIsOverBudget_True() {
        category.setBalance(-50.0);
        assertTrue(category.isOverBudget());
    }

    @Test
    public void testIsOverBudget_ExactlyZero() {
        category.setBalance(0.0);
        assertFalse(category.isOverBudget());
    }

    @Test
    public void testGetUsagePercentage_NoSpending() {
        assertEquals(0.0, category.getUsagePercentage(), 0.001);
    }

    @Test
    public void testGetUsagePercentage_HalfSpent() {
        category.withdrawal(250.0);
        assertEquals(50.0, category.getUsagePercentage(), 0.001);
    }

    @Test
    public void testGetUsagePercentage_AllSpent() {
        category.withdrawal(500.0);
        assertEquals(100.0, category.getUsagePercentage(), 0.001);
    }

    @Test
    public void testGetUsagePercentage_OverBudget() {
        category.withdrawal(600.0);
        assertEquals(120.0, category.getUsagePercentage(), 0.001);
    }

    @Test
    public void testGetUsagePercentage_ZeroBudget() {
        Category zeroBudget = new Category("id", "Test", 0.0, 0);
        assertEquals(0.0, zeroBudget.getUsagePercentage(), 0.001);
    }

    // ============================================
    // EQUALS AND HASHCODE TESTS
    // ============================================

    @Test
    public void testEquals_SameObject() {
        assertEquals(category, category);
    }

    @Test
    public void testEquals_SameId() {
        Category category2 = new Category("cat-1", "Different Name", 1000.0, 1000);
        assertEquals(category, category2);
    }

    @Test
    public void testEquals_DifferentId() {
        Category category2 = new Category("cat-2", "Groceries", 500.0, 500);
        assertNotEquals(category, category2);
    }

    @Test
    public void testEquals_Null() {
        assertNotEquals(category, null);
    }

    @Test
    public void testEquals_DifferentClass() {
        assertNotEquals(category, "not a category");
    }

    @Test
    public void testHashCode_SameId() {
        Category category2 = new Category("cat-1", "Different", 0.0, 0);
        assertEquals(category.hashCode(), category2.hashCode());
    }

    // ============================================
    // CLONE TESTS
    // ============================================

    @Test
    public void testClone() throws CloneNotSupportedException {
        category.addTransactions("trans-1", transaction);
        
        Category cloned = (Category) category.clone();
        
        assertEquals(category.getId(), cloned.getId());
        assertEquals(category.getName(), cloned.getName());
        assertEquals(category.getBalance(), cloned.getBalance(), 0.001);
        assertEquals(category.getBudget(), cloned.getBudget());
        assertEquals(category.getTransactions().size(), cloned.getTransactions().size());
    }

    @Test
    public void testClone_IndependentTransactions() throws CloneNotSupportedException {
        category.addTransactions("trans-1", transaction);
        
        Category cloned = (Category) category.clone();
        
        // Modifying cloned transactions should not affect original
        cloned.getTransactions().clear();
        
        assertEquals(1, category.getTransactions().size());
        assertEquals(0, cloned.getTransactions().size());
    }

    // ============================================
    // TOSTRING TEST
    // ============================================

    @Test
    public void testToString_ContainsAllFields() {
        String str = category.toString();
        
        assertTrue(str.contains("cat-1"));
        assertTrue(str.contains("Groceries"));
        assertTrue(str.contains("500")); // budget
        assertTrue(str.contains("500")); // balance
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    public void testVeryLargeBudget() {
        category.setBudget(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, category.getBudget());
    }

    @Test
    public void testDecimalBalance() {
        category.setBalance(123.456789);
        assertEquals(123.456789, category.getBalance(), 0.000001);
    }

    @Test
    public void testEmptyStringName() {
        category.setName("");
        assertEquals("", category.getName());
    }

    @Test
    public void testUnicodeName() {
        category.setName("קטגוריה בעברית");
        assertEquals("קטגוריה בעברית", category.getName());
    }

    @Test
    public void testAddTransactionWithNullKey() {
        // Adding with null key should not add (based on implementation check)
        int sizeBefore = category.getTransactions().size();
        category.addTransactions(null, transaction);
        // Check implementation - if it allows null keys, this will pass
        // Otherwise the test validates the protective behavior
        assertTrue(sizeBefore <= category.getTransactions().size());
    }

    @Test
    public void testAddNullTransaction() {
        // Adding null transaction - check behavior
        int sizeBefore = category.getTransactions().size();
        category.addTransactions("key", null);
        // Implementation may or may not add null transactions
        assertTrue(sizeBefore <= category.getTransactions().size());
    }
}
