package com.brosh.finance.monthlybudgetsync.objects;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for the Budget class.
 * Tests all getters, setters, constructors, edge cases, and boundary conditions.
 */
public class BudgetTest {

    private Budget budget;

    @Before
    public void setUp() {
        budget = new Budget("Groceries", 500, true, "Walmart", 15, 1);
    }

    // ============================================
    // CONSTRUCTOR TESTS
    // ============================================

    @Test
    public void testDefaultConstructor() {
        Budget defaultBudget = new Budget();
        assertNull(defaultBudget.getId());
        assertNull(defaultBudget.getCategoryName());
        assertEquals(0, defaultBudget.getValue());
        assertFalse(defaultBudget.isConstPayment());
        assertNull(defaultBudget.getShop());
        assertEquals(0, defaultBudget.getChargeDay());
        assertEquals(0, defaultBudget.getCatPriority());
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals("Groceries", budget.getCategoryName());
        assertEquals(500, budget.getValue());
        assertTrue(budget.isConstPayment());
        assertEquals("Walmart", budget.getShop());
        assertEquals(15, budget.getChargeDay());
        assertEquals(1, budget.getCatPriority());
    }

    @Test
    public void testConstructorWithNullValues() {
        Budget nullBudget = new Budget(null, 100, false, null, 10, 0);
        assertNull(nullBudget.getCategoryName());
        assertNull(nullBudget.getShop());
        assertEquals(100, nullBudget.getValue());
    }

    // ============================================
    // VALUE VALIDATION TESTS
    // ============================================

    @Test
    public void testNegativeValueClampedToZero() {
        Budget negativeBudget = new Budget("Test", -100, false, null, 1, 0);
        assertEquals(0, negativeBudget.getValue());
    }

    @Test
    public void testSetNegativeValueClampedToZero() {
        budget.setValue(-500);
        assertEquals(0, budget.getValue());
    }

    @Test
    public void testZeroValueAllowed() {
        budget.setValue(0);
        assertEquals(0, budget.getValue());
    }

    @Test
    public void testLargeValueAllowed() {
        budget.setValue(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, budget.getValue());
    }

    // ============================================
    // CHARGE DAY VALIDATION TESTS
    // ============================================

    @Test
    public void testChargeDayClampedToValidRange_Low() {
        Budget lowDayBudget = new Budget("Test", 100, false, null, 0, 0);
        assertEquals(1, lowDayBudget.getChargeDay());
    }

    @Test
    public void testChargeDayClampedToValidRange_Negative() {
        Budget negativeDayBudget = new Budget("Test", 100, false, null, -5, 0);
        assertEquals(1, negativeDayBudget.getChargeDay());
    }

    @Test
    public void testChargeDayClampedToValidRange_High() {
        Budget highDayBudget = new Budget("Test", 100, false, null, 32, 0);
        assertEquals(31, highDayBudget.getChargeDay());
    }

    @Test
    public void testSetChargeDayClampedToValidRange() {
        budget.setChargeDay(0);
        assertEquals(1, budget.getChargeDay());
        
        budget.setChargeDay(50);
        assertEquals(31, budget.getChargeDay());
    }

    @Test
    public void testValidChargeDays() {
        for (int day = 1; day <= 31; day++) {
            budget.setChargeDay(day);
            assertEquals(day, budget.getChargeDay());
        }
    }

    // ============================================
    // GETTER AND SETTER TESTS
    // ============================================

    @Test
    public void testSetAndGetId() {
        budget.setId("budget-123");
        assertEquals("budget-123", budget.getId());
    }

    @Test
    public void testSetAndGetCategoryName() {
        budget.setCategoryName("Entertainment");
        assertEquals("Entertainment", budget.getCategoryName());
    }

    @Test
    public void testSetAndGetShop() {
        budget.setShop("Amazon");
        assertEquals("Amazon", budget.getShop());
    }

    @Test
    public void testSetAndGetConstPayment() {
        budget.setConstPayment(false);
        assertFalse(budget.isConstPayment());
        
        budget.setConstPayment(true);
        assertTrue(budget.isConstPayment());
    }

    @Test
    public void testSetAndGetCatPriority() {
        budget.setCatPriority(5);
        assertEquals(5, budget.getCatPriority());
        
        budget.setCatPriority(-1);
        assertEquals(-1, budget.getCatPriority());
    }

    // ============================================
    // EQUALS AND HASHCODE TESTS
    // ============================================

    @Test
    public void testEquals_SameObject() {
        assertEquals(budget, budget);
    }

    @Test
    public void testEquals_EqualObjects() {
        Budget budget2 = new Budget("Groceries", 500, true, "Walmart", 15, 1);
        assertEquals(budget, budget2);
    }

    @Test
    public void testEquals_DifferentCategoryName() {
        Budget budget2 = new Budget("Food", 500, true, "Walmart", 15, 1);
        assertNotEquals(budget, budget2);
    }

    @Test
    public void testEquals_DifferentValue() {
        Budget budget2 = new Budget("Groceries", 600, true, "Walmart", 15, 1);
        assertNotEquals(budget, budget2);
    }

    @Test
    public void testEquals_DifferentConstPayment() {
        Budget budget2 = new Budget("Groceries", 500, false, "Walmart", 15, 1);
        assertNotEquals(budget, budget2);
    }

    @Test
    public void testEquals_DifferentShop() {
        Budget budget2 = new Budget("Groceries", 500, true, "Target", 15, 1);
        assertNotEquals(budget, budget2);
    }

    @Test
    public void testEquals_DifferentChargeDay() {
        Budget budget2 = new Budget("Groceries", 500, true, "Walmart", 20, 1);
        assertNotEquals(budget, budget2);
    }

    @Test
    public void testEquals_DifferentPriority_StillEquals() {
        // catPriority is NOT part of equals comparison
        Budget budget2 = new Budget("Groceries", 500, true, "Walmart", 15, 99);
        assertEquals(budget, budget2);
    }

    @Test
    public void testEquals_Null() {
        assertNotEquals(budget, null);
    }

    @Test
    public void testEquals_DifferentClass() {
        assertNotEquals(budget, "not a budget");
    }

    @Test
    public void testHashCode_EqualObjects() {
        Budget budget2 = new Budget("Groceries", 500, true, "Walmart", 15, 1);
        assertEquals(budget.hashCode(), budget2.hashCode());
    }

    @Test
    public void testHashCode_Consistent() {
        int hash1 = budget.hashCode();
        int hash2 = budget.hashCode();
        assertEquals(hash1, hash2);
    }

    // ============================================
    // TOSTRING TEST
    // ============================================

    @Test
    public void testToString_ContainsAllFields() {
        budget.setId("test-id");
        String str = budget.toString();
        
        assertTrue(str.contains("test-id"));
        assertTrue(str.contains("Groceries"));
        assertTrue(str.contains("500"));
        assertTrue(str.contains("true"));
        assertTrue(str.contains("Walmart"));
        assertTrue(str.contains("15"));
    }

    // ============================================
    // SERIALIZATION TEST
    // ============================================

    @Test
    public void testSerialVersionUID() {
        // Ensure the class has a serialVersionUID for proper serialization
        try {
            java.lang.reflect.Field field = Budget.class.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            long serialVersionUID = field.getLong(null);
            assertEquals(1L, serialVersionUID);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("serialVersionUID field not found or not accessible");
        }
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    public void testEmptyStringCategoryName() {
        budget.setCategoryName("");
        assertEquals("", budget.getCategoryName());
    }

    @Test
    public void testWhitespaceCategoryName() {
        budget.setCategoryName("   ");
        assertEquals("   ", budget.getCategoryName());
    }

    @Test
    public void testSpecialCharactersInCategoryName() {
        budget.setCategoryName("Category#1_Test@Special!");
        assertEquals("Category#1_Test@Special!", budget.getCategoryName());
    }

    @Test
    public void testUnicodeCategoryName() {
        budget.setCategoryName("קטגוריה בעברית");
        assertEquals("קטגוריה בעברית", budget.getCategoryName());
    }

    @Test
    public void testMaxIntegerValue() {
        budget.setValue(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, budget.getValue());
    }
}
