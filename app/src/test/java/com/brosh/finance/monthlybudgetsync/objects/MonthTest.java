package com.brosh.finance.monthlybudgetsync.objects;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive unit tests for the Month class.
 * Tests all getters, setters, constructors, category operations,
 * and active status logic.
 */
public class MonthTest {

    private Month month;
    private Category category;

    @Before
    public void setUp() {
        // Create a month for current year-month for testing active status
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int monthNum = cal.get(Calendar.MONTH) + 1;
        String yearMonth = year + "-" + (monthNum < 10 ? "0" + monthNum : monthNum);
        
        month = new Month(yearMonth, 1L, 15);
        category = new Category("cat-1", "Groceries", 500.0, 500);
    }

    // ============================================
    // CONSTRUCTOR TESTS
    // ============================================

    @Test
    public void testDefaultConstructor() {
        Month defaultMonth = new Month();
        assertNull(defaultMonth.getId());
        assertNull(defaultMonth.getRefMonth());
        assertNull(defaultMonth.getYearMonth());
        assertFalse(defaultMonth.isActive());
        assertEquals(0, defaultMonth.getChargeDay());
        assertNotNull(defaultMonth.getCategories());
        assertTrue(defaultMonth.getCategories().isEmpty());
        assertEquals(1, defaultMonth.getTranIdNumerator());
        assertEquals(0, defaultMonth.getBudgetNumber());
    }

    @Test
    public void testParameterizedConstructor() {
        assertNotNull(month.getId());
        assertNotNull(month.getRefMonth());
        assertNotNull(month.getYearMonth());
        assertEquals(15, month.getChargeDay());
        assertEquals(1L, month.getBudgetNumber());
        assertNotNull(month.getCategories());
        assertTrue(month.getCategories().isEmpty());
        assertEquals(1, month.getTranIdNumerator());
    }

    @Test
    public void testConstructor_SetsActiveForCurrentMonth() {
        // Current month should be active
        assertTrue(month.isActive());
    }

    @Test
    public void testConstructor_PastMonthNotActive() {
        Month pastMonth = new Month("2020-01", 1L, 15);
        assertFalse(pastMonth.isActive());
    }

    @Test
    public void testConstructor_FutureMonthNotActive() {
        Month futureMonth = new Month("2099-12", 1L, 15);
        assertFalse(futureMonth.isActive());
    }

    // ============================================
    // GETTER AND SETTER TESTS
    // ============================================

    @Test
    public void testSetAndGetId() {
        month.setId("new-id");
        assertEquals("new-id", month.getId());
    }

    @Test
    public void testSetAndGetYearMonth() {
        month.setYearMonth("2024-06");
        assertEquals("2024-06", month.getYearMonth());
    }

    @Test
    public void testSetAndGetRefMonth() {
        Date date = new Date();
        month.setRefMonth(date);
        assertEquals(date, month.getRefMonth());
    }

    @Test
    public void testSetAndGetChargeDay() {
        month.setChargeDay(25);
        assertEquals(25, month.getChargeDay());
    }

    @Test
    public void testSetAndGetBudgetNumber() {
        month.setBudgetNumber(5L);
        assertEquals(5L, month.getBudgetNumber());
    }

    @Test
    public void testSetAndGetTranIdNumerator() {
        month.setTranIdNumerator(100);
        assertEquals(100, month.getTranIdNumerator());
    }

    @Test
    public void testSetAndGetActive() {
        month.setActive(false);
        assertFalse(month.isActive());
        
        month.setActive(true);
        assertTrue(month.isActive());
    }

    // ============================================
    // CATEGORY OPERATION TESTS
    // ============================================

    @Test
    public void testAddCategory() {
        month.addCategory("cat-1", category);
        assertEquals(1, month.getCategoryCount());
        assertTrue(month.hasCategory("cat-1"));
        assertEquals(category, month.getCategory("cat-1"));
    }

    @Test
    public void testAddMultipleCategories() {
        Category cat2 = new Category("cat-2", "Entertainment", 200.0, 200);
        
        month.addCategory("cat-1", category);
        month.addCategory("cat-2", cat2);
        
        assertEquals(2, month.getCategoryCount());
        assertTrue(month.hasCategory("cat-1"));
        assertTrue(month.hasCategory("cat-2"));
    }

    @Test
    public void testAddCategoryWithNullKey() {
        month.addCategory(null, category);
        assertEquals(0, month.getCategoryCount());
    }

    @Test
    public void testAddNullCategory() {
        month.addCategory("cat-1", null);
        assertFalse(month.hasCategory("cat-1"));
    }

    @Test
    public void testGetCategory() {
        month.addCategory("cat-1", category);
        assertEquals(category, month.getCategory("cat-1"));
    }

    @Test
    public void testGetNonExistentCategory() {
        assertNull(month.getCategory("non-existent"));
    }

    @Test
    public void testGetCategoryWithNullId() {
        assertNull(month.getCategory(null));
    }

    @Test
    public void testHasCategory() {
        assertFalse(month.hasCategory("cat-1"));
        month.addCategory("cat-1", category);
        assertTrue(month.hasCategory("cat-1"));
    }

    @Test
    public void testHasCategoryWithNullId() {
        assertFalse(month.hasCategory(null));
    }

    @Test
    public void testGetCategoryCount() {
        assertEquals(0, month.getCategoryCount());
        month.addCategory("cat-1", category);
        assertEquals(1, month.getCategoryCount());
    }

    @Test
    public void testUpdateSpecificCategory() {
        month.addCategory("cat-1", category);
        
        Category updatedCategory = new Category("cat-1", "Updated Groceries", 1000.0, 1000);
        month.updateSpecificCategory("cat-1", updatedCategory);
        
        assertEquals("Updated Groceries", month.getCategory("cat-1").getName());
        assertEquals(1000.0, month.getCategory("cat-1").getBalance(), 0.001);
    }

    @Test
    public void testUpdateSpecificCategoryWithNullId() {
        month.updateSpecificCategory(null, category);
        assertEquals(0, month.getCategoryCount());
    }

    @Test
    public void testUpdateSpecificCategoryWithNullCategory() {
        month.addCategory("cat-1", category);
        month.updateSpecificCategory("cat-1", null);
        // Null category should not update
        assertEquals(category, month.getCategory("cat-1"));
    }

    @Test
    public void testSetCategories() {
        Map<String, Category> categories = new HashMap<>();
        categories.put("cat-1", category);
        
        month.setCategories(categories);
        
        assertEquals(1, month.getCategoryCount());
        assertTrue(month.hasCategory("cat-1"));
    }

    @Test
    public void testSetCategoriesWithNull() {
        month.addCategory("cat-1", category);
        month.setCategories(null);
        
        assertNotNull(month.getCategories());
        assertEquals(0, month.getCategoryCount());
    }

    @Test
    public void testGetCategoriesReadOnly() {
        month.addCategory("cat-1", category);
        
        Map<String, Category> readOnly = month.getCategoriesReadOnly();
        
        try {
            readOnly.put("cat-2", new Category("cat-2", "Test", 100.0, 100));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    // ============================================
    // TRANSACTION ID NUMERATOR TESTS
    // ============================================

    @Test
    public void testGetNextTransactionId() {
        assertEquals(1, month.getTranIdNumerator());
        
        int id1 = month.getNextTransactionId();
        assertEquals(1, id1);
        assertEquals(2, month.getTranIdNumerator());
        
        int id2 = month.getNextTransactionId();
        assertEquals(2, id2);
        assertEquals(3, month.getTranIdNumerator());
    }

    @Test
    public void testGetNextTransactionId_Sequential() {
        for (int i = 1; i <= 100; i++) {
            assertEquals(i, month.getNextTransactionId());
        }
        assertEquals(101, month.getTranIdNumerator());
    }

    // ============================================
    // ACTIVE STATUS TESTS
    // ============================================

    @Test
    public void testSetIsActive_UpdatesBasedOnRefMonth() {
        Month pastMonth = new Month("2020-01", 1L, 15);
        assertFalse(pastMonth.isActive());
        
        pastMonth.setIsActive();
        assertFalse(pastMonth.isActive()); // Still not active (past month)
    }

    // ============================================
    // EQUALS AND HASHCODE TESTS
    // ============================================

    @Test
    public void testEquals_SameObject() {
        assertEquals(month, month);
    }

    @Test
    public void testEquals_SameIdAndYearMonth() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int monthNum = cal.get(Calendar.MONTH) + 1;
        String yearMonth = year + "-" + (monthNum < 10 ? "0" + monthNum : monthNum);
        
        Month month2 = new Month(yearMonth, 99L, 1);
        assertEquals(month, month2);
    }

    @Test
    public void testEquals_DifferentYearMonth() {
        Month month2 = new Month("2020-01", 1L, 15);
        assertNotEquals(month, month2);
    }

    @Test
    public void testEquals_Null() {
        assertNotEquals(month, null);
    }

    @Test
    public void testEquals_DifferentClass() {
        assertNotEquals(month, "not a month");
    }

    @Test
    public void testHashCode_SameIdAndYearMonth() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int monthNum = cal.get(Calendar.MONTH) + 1;
        String yearMonth = year + "-" + (monthNum < 10 ? "0" + monthNum : monthNum);
        
        Month month2 = new Month(yearMonth, 99L, 1);
        assertEquals(month.hashCode(), month2.hashCode());
    }

    // ============================================
    // TOSTRING TEST
    // ============================================

    @Test
    public void testToString_ContainsAllFields() {
        month.addCategory("cat-1", category);
        String str = month.toString();
        
        assertTrue(str.contains(month.getId()));
        assertTrue(str.contains(month.getYearMonth()));
        assertTrue(str.contains("1")); // categories count
    }

    // ============================================
    // SERIALIZATION TEST
    // ============================================

    @Test
    public void testSerialVersionUID() {
        try {
            java.lang.reflect.Field field = Month.class.getDeclaredField("serialVersionUID");
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
    public void testNegativeTranIdNumerator() {
        month.setTranIdNumerator(-5);
        assertEquals(-5, month.getTranIdNumerator());
    }

    @Test
    public void testZeroChargeDay() {
        month.setChargeDay(0);
        assertEquals(0, month.getChargeDay());
    }

    @Test
    public void testLargeBudgetNumber() {
        month.setBudgetNumber(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, month.getBudgetNumber());
    }

    @Test
    public void testYearMonthFormats() {
        String[] validFormats = {"2024-01", "2024-12", "1999-06"};
        for (String ym : validFormats) {
            Month testMonth = new Month(ym, 1L, 15);
            assertEquals(ym, testMonth.getYearMonth());
        }
    }
}
