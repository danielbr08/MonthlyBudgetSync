package com.brosh.finance.monthlybudgetsync.objects;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * Comprehensive unit tests for the Transaction class.
 * Tests all getters, setters, constructors, and helper methods.
 */
public class TransactionTest {

    private Transaction transaction;
    private Date testDate;

    @Before
    public void setUp() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        testDate = cal.getTime();
        
        transaction = new Transaction("trans-1", 1, "Groceries", "Cash", "Walmart", testDate, 50.0);
    }

    // ============================================
    // CONSTRUCTOR TESTS
    // ============================================

    @Test
    public void testDefaultConstructor() {
        Transaction defaultTrans = new Transaction();
        assertNull(defaultTrans.getId());
        assertEquals(0, defaultTrans.getIdPerMonth());
        assertNull(defaultTrans.getCategory());
        assertNull(defaultTrans.getPaymentMethod());
        assertNull(defaultTrans.getShop());
        assertNull(defaultTrans.getPayDate());
        assertEquals(0.0, defaultTrans.getPrice(), 0.001);
        assertNull(defaultTrans.getRegistrationDate());
        assertFalse(defaultTrans.isDeleted());
        assertNull(defaultTrans.getComment());
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals("trans-1", transaction.getId());
        assertEquals(1, transaction.getIdPerMonth());
        assertEquals("Groceries", transaction.getCategory());
        assertEquals("Cash", transaction.getPaymentMethod());
        assertEquals("Walmart", transaction.getShop());
        assertEquals(testDate, transaction.getPayDate());
        assertEquals(50.0, transaction.getPrice(), 0.001);
        assertNotNull(transaction.getRegistrationDate());
        assertFalse(transaction.isDeleted());
    }

    @Test
    public void testConstructorWithNullValues() {
        Transaction nullTrans = new Transaction(null, 1, null, null, null, null, 100.0);
        assertNull(nullTrans.getId());
        assertNull(nullTrans.getCategory());
        assertNull(nullTrans.getPaymentMethod());
        assertNull(nullTrans.getShop());
        assertNull(nullTrans.getPayDate());
        assertEquals(100.0, nullTrans.getPrice(), 0.001);
    }

    @Test
    public void testConstructorNegativePriceClampedToZero() {
        Transaction negativePriceTrans = new Transaction("id", 1, "Cat", "Cash", "Shop", testDate, -50.0);
        assertEquals(0.0, negativePriceTrans.getPrice(), 0.001);
    }

    // ============================================
    // GETTER AND SETTER TESTS
    // ============================================

    @Test
    public void testSetAndGetId() {
        transaction.setId("new-id");
        assertEquals("new-id", transaction.getId());
    }

    @Test
    public void testSetAndGetIdPerMonth() {
        transaction.setIdPerMonth(99);
        assertEquals(99, transaction.getIdPerMonth());
    }

    @Test
    public void testSetAndGetCategory() {
        transaction.setCategory("Entertainment");
        assertEquals("Entertainment", transaction.getCategory());
    }

    @Test
    public void testSetAndGetPaymentMethod() {
        transaction.setPaymentMethod("Credit Card");
        assertEquals("Credit Card", transaction.getPaymentMethod());
    }

    @Test
    public void testSetAndGetShop() {
        transaction.setShop("Target");
        assertEquals("Target", transaction.getShop());
    }

    @Test
    public void testSetAndGetPayDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.FEBRUARY, 20);
        Date newDate = cal.getTime();
        
        transaction.setPayDate(newDate);
        assertEquals(newDate, transaction.getPayDate());
    }

    @Test
    public void testSetAndGetPrice() {
        transaction.setPrice(150.75);
        assertEquals(150.75, transaction.getPrice(), 0.001);
    }

    @Test
    public void testSetNegativePriceClampedToZero() {
        transaction.setPrice(-100.0);
        assertEquals(0.0, transaction.getPrice(), 0.001);
    }

    @Test
    public void testSetAndGetRegistrationDate() {
        Date regDate = new Date();
        transaction.setRegistrationDate(regDate);
        assertEquals(regDate, transaction.getRegistrationDate());
    }

    @Test
    public void testSetAndGetDeleted() {
        transaction.setDeleted(true);
        assertTrue(transaction.isDeleted());
        
        transaction.setDeleted(false);
        assertFalse(transaction.isDeleted());
    }

    @Test
    public void testSetAndGetComment() {
        transaction.setComment("Weekly groceries");
        assertEquals("Weekly groceries", transaction.getComment());
    }

    // ============================================
    // HELPER METHOD TESTS
    // ============================================

    @Test
    public void testIsActive_NotDeleted() {
        assertFalse(transaction.isDeleted());
        assertTrue(transaction.isActive());
    }

    @Test
    public void testIsActive_Deleted() {
        transaction.setDeleted(true);
        assertTrue(transaction.isDeleted());
        assertFalse(transaction.isActive());
    }

    @Test
    public void testMarkDeleted() {
        assertFalse(transaction.isDeleted());
        transaction.markDeleted();
        assertTrue(transaction.isDeleted());
    }

    @Test
    public void testRestore() {
        transaction.markDeleted();
        assertTrue(transaction.isDeleted());
        
        transaction.restore();
        assertFalse(transaction.isDeleted());
    }

    @Test
    public void testFormatDateFields_WithNonNullDates() {
        // formatDateFields should clone dates
        Date payDate = transaction.getPayDate();
        Date regDate = transaction.getRegistrationDate();
        
        transaction.formatDateFields();
        
        // Dates should still be valid (cloned)
        assertNotNull(transaction.getPayDate());
        assertNotNull(transaction.getRegistrationDate());
    }

    @Test
    public void testFormatDateFields_WithNullDates() {
        transaction.setPayDate(null);
        transaction.setRegistrationDate(null);
        
        // Should not throw
        transaction.formatDateFields();
        
        assertNull(transaction.getPayDate());
        assertNull(transaction.getRegistrationDate());
    }

    // ============================================
    // EQUALS AND HASHCODE TESTS
    // ============================================

    @Test
    public void testEquals_SameObject() {
        assertEquals(transaction, transaction);
    }

    @Test
    public void testEquals_SameId() {
        // Equals is based on ID only
        Transaction trans2 = new Transaction("trans-1", 99, "Different", "Different", "Different", null, 999.0);
        assertEquals(transaction, trans2);
    }

    @Test
    public void testEquals_DifferentId() {
        Transaction trans2 = new Transaction("trans-2", 1, "Groceries", "Cash", "Walmart", testDate, 50.0);
        assertNotEquals(transaction, trans2);
    }

    @Test
    public void testEquals_NullId() {
        Transaction trans1 = new Transaction(null, 1, "Cat", "Method", "Shop", null, 10.0);
        Transaction trans2 = new Transaction(null, 2, "Other", "Other", "Other", null, 20.0);
        trans1.setId(null);
        trans2.setId(null);
        // Both null IDs should be equal by Objects.equals
        assertEquals(trans1, trans2);
    }

    @Test
    public void testEquals_Null() {
        assertNotEquals(transaction, null);
    }

    @Test
    public void testEquals_DifferentClass() {
        assertNotEquals(transaction, "not a transaction");
    }

    @Test
    public void testHashCode_SameId() {
        Transaction trans2 = new Transaction("trans-1", 99, "Different", "Different", "Different", null, 999.0);
        assertEquals(transaction.hashCode(), trans2.hashCode());
    }

    @Test
    public void testHashCode_Consistent() {
        int hash1 = transaction.hashCode();
        int hash2 = transaction.hashCode();
        assertEquals(hash1, hash2);
    }

    // ============================================
    // TOSTRING TEST
    // ============================================

    @Test
    public void testToString_ContainsAllFields() {
        transaction.setComment("Test comment");
        String str = transaction.toString();
        
        assertTrue(str.contains("trans-1"));
        assertTrue(str.contains("1")); // idPerMonth
        assertTrue(str.contains("Groceries"));
        assertTrue(str.contains("Walmart"));
        assertTrue(str.contains("50")); // price
    }

    // ============================================
    // SERIALIZATION TEST
    // ============================================

    @Test
    public void testSerialVersionUID() {
        try {
            java.lang.reflect.Field field = Transaction.class.getDeclaredField("serialVersionUID");
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
    public void testZeroPrice() {
        transaction.setPrice(0.0);
        assertEquals(0.0, transaction.getPrice(), 0.001);
    }

    @Test
    public void testVeryLargePrice() {
        transaction.setPrice(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, transaction.getPrice(), 0.001);
    }

    @Test
    public void testDecimalPrice() {
        transaction.setPrice(123.456789);
        assertEquals(123.456789, transaction.getPrice(), 0.000001);
    }

    @Test
    public void testEmptyStringFields() {
        transaction.setCategory("");
        transaction.setPaymentMethod("");
        transaction.setShop("");
        transaction.setComment("");
        
        assertEquals("", transaction.getCategory());
        assertEquals("", transaction.getPaymentMethod());
        assertEquals("", transaction.getShop());
        assertEquals("", transaction.getComment());
    }

    @Test
    public void testUnicodeFields() {
        transaction.setCategory("קטגוריה בעברית");
        transaction.setShop("חנות בעברית");
        transaction.setComment("הערה בעברית");
        
        assertEquals("קטגוריה בעברית", transaction.getCategory());
        assertEquals("חנות בעברית", transaction.getShop());
        assertEquals("הערה בעברית", transaction.getComment());
    }

    @Test
    public void testSpecialCharactersInFields() {
        transaction.setCategory("Category#1@Test!");
        transaction.setShop("Shop/Store\\Name");
        
        assertEquals("Category#1@Test!", transaction.getCategory());
        assertEquals("Shop/Store\\Name", transaction.getShop());
    }

    @Test
    public void testNegativeIdPerMonth() {
        transaction.setIdPerMonth(-5);
        assertEquals(-5, transaction.getIdPerMonth());
    }

    @Test
    public void testMarkDeletedMultipleTimes() {
        transaction.markDeleted();
        transaction.markDeleted();
        assertTrue(transaction.isDeleted());
    }

    @Test
    public void testRestoreMultipleTimes() {
        transaction.markDeleted();
        transaction.restore();
        transaction.restore();
        assertFalse(transaction.isDeleted());
    }
}
