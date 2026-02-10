package com.brosh.finance.monthlybudgetsync;

import static org.junit.Assert.*;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.*;
import com.brosh.finance.monthlybudgetsync.utils.*;

import org.junit.Test;

import java.util.*;

/**
 * Tests for Firebase data integrity and synchronization scenarios.
 * 
 * Covers:
 * - Firebase key validation (illegal characters)
 * - Data consistency between related objects
 * - Sharing scenarios
 * - Email-to-UID mapping
 * - Partial data scenarios (network failures)
 */
public class FirebaseDataIntegrityTest {

    // ============================================
    // FIREBASE KEY VALIDATION TESTS
    // ============================================

    @Test
    public void firebaseKey_DotCharacter() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test.key"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("email@example.com")); // Contains dot
    }

    @Test
    public void firebaseKey_DollarSign() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test$key"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("$variable"));
    }

    @Test
    public void firebaseKey_HashCharacter() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test#key"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("#hashtag"));
    }

    @Test
    public void firebaseKey_Brackets() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test[0]"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("array[index]"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("[start"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("end]"));
    }

    @Test
    public void firebaseKey_ForwardSlash() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("path/to/data"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("/root"));
    }

    @Test
    public void firebaseKey_ValidKeys() {
        assertFalse(ValidationUtil.containsIllegalFirebaseChars("valid-key"));
        assertFalse(ValidationUtil.containsIllegalFirebaseChars("valid_key_123"));
        assertFalse(ValidationUtil.containsIllegalFirebaseChars("CamelCaseKey"));
        assertFalse(ValidationUtil.containsIllegalFirebaseChars("key123"));
    }

    @Test
    public void firebaseKey_AllIllegalCharsTogether() {
        String allIllegal = ".#$[]/ mixed with valid";
        assertTrue(ValidationUtil.containsIllegalFirebaseChars(allIllegal));
    }

    // ============================================
    // EMAIL TO UID MAPPING TESTS
    // ============================================

    @Test
    public void emailMapping_DotToComma() {
        assertEquals("test@example,com", TextUtil.getEmailComma("test@example.com"));
        assertEquals("user,name@domain,co,uk", TextUtil.getEmailComma("user.name@domain.co.uk"));
    }

    @Test
    public void emailMapping_NoDots() {
        assertEquals("test@examplecom", TextUtil.getEmailComma("test@examplecom"));
    }

    @Test
    public void emailMapping_Roundtrip() {
        String original = "test@example.com";
        String converted = TextUtil.getEmailComma(original);
        String restored = converted.replace(',', '.');
        
        assertEquals(original, restored);
    }

    @Test
    public void emailMapping_SpecialEmails() {
        // Email with multiple dots
        assertEquals("a,b,c@x,y,z", TextUtil.getEmailComma("a.b.c@x.y.z"));
        
        // Email with plus sign (should preserve)
        assertEquals("user+tag@example,com", TextUtil.getEmailComma("user+tag@example.com"));
    }

    // ============================================
    // SHARING SCENARIOS TESTS
    // ============================================

    @Test
    public void sharing_OwnerUser() {
        User owner = new User("owner-uid-123", "Owner Name", "owner@test.com", "+123", null);
        
        assertTrue(owner.isOwner());
        assertEquals("owner-uid-123", owner.getUid());
        assertEquals("owner-uid-123", owner.getDbKey()); // Defaults to UID
        assertEquals("owner-uid-123", owner.getOwnerUid());
    }

    @Test
    public void sharing_GuestUser() {
        User guest = new User("guest-uid-456", "Guest Name", "guest@test.com", "+456", "owner-uid-123");
        guest.setOwnerUid("owner-uid-123");
        
        assertFalse(guest.isOwner());
        assertEquals("guest-uid-456", guest.getUid());
        assertEquals("owner-uid-123", guest.getDbKey()); // Uses owner's dbKey
        assertEquals("owner-uid-123", guest.getOwnerUid());
    }

    @Test
    public void sharing_UserBecomesGuest() {
        User user = new User("user-uid", "Name", "test@test.com", null, null);
        
        // Initially owner
        assertTrue(user.isOwner());
        
        // Accept share invitation
        user.setDbKey("other-owner-uid");
        user.setOwnerUid("other-owner-uid");
        
        // Now guest
        assertFalse(user.isOwner());
    }

    @Test
    public void sharing_GuestBecomesOwner() {
        User user = new User("user-uid", "Name", "test@test.com", null, "owner-uid");
        user.setOwnerUid("owner-uid");
        
        // Initially guest
        assertFalse(user.isOwner());
        
        // Stop sharing (back to own budget)
        user.setDbKey("user-uid");
        user.setOwnerUid("user-uid");
        
        // Now owner again
        assertTrue(user.isOwner());
    }

    // ============================================
    // DATA CONSISTENCY TESTS
    // ============================================

    @Test
    public void dataConsistency_CategoryTransactionBalance() {
        Category category = new Category("cat-1", "Groceries", 500.0, 500);
        
        // Add transactions and track balance
        double expectedBalance = 500.0;
        
        Transaction t1 = new Transaction("t1", 1, "Groceries", "Cash", "Shop1", null, 100.0);
        category.addTransactions("t1", t1);
        category.withdrawal(100.0);
        expectedBalance -= 100.0;
        
        Transaction t2 = new Transaction("t2", 2, "Groceries", "Card", "Shop2", null, 50.0);
        category.addTransactions("t2", t2);
        category.withdrawal(50.0);
        expectedBalance -= 50.0;
        
        assertEquals(expectedBalance, category.getBalance(), 0.001);
        assertEquals(2, category.getTransactions().size());
    }

    @Test
    public void dataConsistency_MonthCategoryRelationship() {
        Month month = new Month("2024-06", 1L, 15);
        
        Category cat1 = new Category("cat-1", "Groceries", 500.0, 500);
        Category cat2 = new Category("cat-2", "Entertainment", 200.0, 200);
        
        month.addCategory("cat-1", cat1);
        month.addCategory("cat-2", cat2);
        
        assertEquals(2, month.getCategoryCount());
        assertTrue(month.hasCategory("cat-1"));
        assertTrue(month.hasCategory("cat-2"));
        assertEquals(cat1, month.getCategory("cat-1"));
        assertEquals(cat2, month.getCategory("cat-2"));
    }

    @Test
    public void dataConsistency_BudgetCategoryMapping() {
        // Budget defines the template
        Budget budget = new Budget("Groceries", 500, true, "Walmart", 15, 1);
        
        // Category is an instance for a specific month
        Category category = new Category("cat-1", budget.getCategoryName(), budget.getValue(), budget.getValue());
        
        assertEquals(budget.getCategoryName(), category.getName());
        assertEquals(budget.getValue(), category.getBudget());
        assertEquals(budget.getValue(), category.getBalance(), 0.001);
    }

    // ============================================
    // PARTIAL DATA SCENARIOS (Network Failures)
    // ============================================

    @Test
    public void partialData_MonthWithNoCategories() {
        Month month = new Month("2024-06", 1L, 15);
        
        // Month exists but has no categories (partial sync)
        assertEquals(0, month.getCategoryCount());
        assertFalse(month.hasCategory("any"));
        assertNotNull(month.getCategories());
        assertNotNull(month.getCategoriesReadOnly());
    }

    @Test
    public void partialData_CategoryWithNoTransactions() {
        Category category = new Category("cat-1", "Test", 100.0, 100);
        
        assertEquals(0, category.getTransactions().size());
        assertNull(category.getTransaction("any"));
        assertFalse(category.hasTransaction("any"));
    }

    @Test
    public void partialData_UserWithMissingFields() {
        User user = new User();
        
        // All fields null (partial data)
        assertNull(user.getUid());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPhone());
        assertNull(user.getDbKey());
        assertNull(user.getOwnerUid());
        
        // Should not crash
        assertNotNull(user.getUserSettings());
        assertFalse(user.isOwner()); // Null UID can't be owner
        assertEquals("", user.getEmailComma()); // Handles null email
    }

    @Test
    public void partialData_TransactionMissingDates() {
        Transaction trans = new Transaction();
        
        assertNull(trans.getPayDate());
        assertNull(trans.getRegistrationDate());
        
        // formatDateFields should handle nulls
        trans.formatDateFields();
        
        assertNull(trans.getPayDate());
        assertNull(trans.getRegistrationDate());
    }

    @Test
    public void partialData_BudgetMissingOptionalFields() {
        Budget budget = new Budget();
        budget.setCategoryName("Test");
        budget.setValue(100);
        // shop and other fields are null
        
        assertNull(budget.getShop());
        assertNull(budget.getId());
        
        // Should still function
        assertEquals("Test", budget.getCategoryName());
        assertEquals(100, budget.getValue());
    }

    // ============================================
    // TRANSACTION ID NUMERATOR TESTS
    // ============================================

    @Test
    public void tranIdNumerator_Sequential() {
        Month month = new Month("2024-06", 1L, 15);
        
        assertEquals(1, month.getNextTransactionId());
        assertEquals(2, month.getNextTransactionId());
        assertEquals(3, month.getNextTransactionId());
        assertEquals(4, month.getTranIdNumerator());
    }

    @Test
    public void tranIdNumerator_ConcurrentCreation() {
        Month month = new Month("2024-06", 1L, 15);
        
        List<Integer> ids = new ArrayList<>();
        
        // Simulate creating many transactions
        for (int i = 0; i < 100; i++) {
            ids.add(month.getNextTransactionId());
        }
        
        // All IDs should be unique
        Set<Integer> uniqueIds = new HashSet<>(ids);
        assertEquals(100, uniqueIds.size());
    }

    @Test
    public void tranIdNumerator_AfterReset() {
        Month month = new Month("2024-06", 1L, 15);
        
        // Get some IDs
        month.getNextTransactionId();
        month.getNextTransactionId();
        
        // Manually reset (like after month rollover)
        month.setTranIdNumerator(1);
        
        assertEquals(1, month.getNextTransactionId());
    }

    // ============================================
    // BUDGET NUMBER TESTS
    // ============================================

    @Test
    public void budgetNumber_Versioning() {
        Month month1 = new Month("2024-06", 1L, 15);
        assertEquals(1L, month1.getBudgetNumber());
        
        // Budget template updated
        Month month2 = new Month("2024-07", 2L, 15);
        assertEquals(2L, month2.getBudgetNumber());
    }

    @Test
    public void budgetNumber_LargeValue() {
        Month month = new Month("2024-06", Long.MAX_VALUE, 15);
        assertEquals(Long.MAX_VALUE, month.getBudgetNumber());
    }

    // ============================================
    // CHARGE DAY TESTS
    // ============================================

    @Test
    public void chargeDay_ValidRange() {
        for (int day = 1; day <= 31; day++) {
            Budget budget = new Budget("Test", 100, false, null, day, 0);
            assertEquals(day, budget.getChargeDay());
        }
    }

    @Test
    public void chargeDay_MonthComparison() {
        Month month = new Month("2024-06", 1L, 15);
        assertEquals(15, month.getChargeDay());
        
        Budget budget = new Budget("Test", 100, true, "Shop", 15, 0);
        assertEquals(budget.getChargeDay(), month.getChargeDay());
    }

    // ============================================
    // USER SETTINGS TESTS
    // ============================================

    @Test
    public void userSettings_DefaultValues() {
        User user = new User("uid", "Name", "email@test.com", null, null);
        
        UserSettings settings = user.getUserSettings();
        assertNotNull(settings);
    }

    @Test
    public void userSettings_NullProtection() {
        User user = new User("uid", "Name", "email@test.com", null, null);
        
        // Setting null should replace with default
        user.setUserSettings(null);
        
        assertNotNull(user.getUserSettings());
    }

    // ============================================
    // YEAR-MONTH KEY TESTS
    // ============================================

    @Test
    public void yearMonthKey_Format() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15);
        
        String key = DateUtil.getYearMonth(cal.getTime(), Config.SEPARATOR);
        assertEquals("2024-01", key);
    }

    @Test
    public void yearMonthKey_AllMonths() {
        String[] expected = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        
        for (int m = 0; m < 12; m++) {
            Calendar cal = Calendar.getInstance();
            cal.set(2024, m, 15);
            
            String key = DateUtil.getYearMonth(cal.getTime(), "-");
            assertTrue("Key should contain " + expected[m], key.endsWith(expected[m]));
        }
    }

    @Test
    public void yearMonthKey_UsedAsMonthId() {
        String yearMonth = "2024-06";
        Month month = new Month(yearMonth, 1L, 15);
        
        assertEquals(yearMonth, month.getId());
        assertEquals(yearMonth, month.getYearMonth());
    }

    // ============================================
    // DEFINITIONS CONSTANTS TESTS
    // ============================================

    @Test
    public void definitions_NodeNames() {
        assertNotNull(Definitions.CATEGORIES);
        assertNotNull(Definitions.TRANSACTIONS);
        assertNotNull(Definitions.MONTHLY_BUDGET);
        assertNotNull(Definitions.BUDGETS);
        assertNotNull(Definitions.MONTHS);
        assertNotNull(Definitions.USERS);
        assertNotNull(Definitions.SHARES);
        assertNotNull(Definitions.OWNERS);
    }

    @Test
    public void definitions_FieldNames() {
        assertNotNull(Definitions.BALANCE);
        assertNotNull(Definitions.BUDGET);
        assertNotNull(Definitions.NAME);
        assertNotNull(Definitions.DELETED);
        assertNotNull(Definitions.CURRENCY);
    }

    @Test
    public void definitions_SortConstants() {
        assertTrue(Definitions.SORT_BY_ID > 0);
        assertTrue(Definitions.SORT_BY_CATEGORY > 0);
        assertTrue(Definitions.SORT_BY_PAYMENT_METHOD > 0);
        assertTrue(Definitions.SORT_BY_STORE > 0);
        assertTrue(Definitions.SORT_BY_CHARGE_DATE > 0);
        assertTrue(Definitions.SORT_BY_PRICE > 0);
        assertTrue(Definitions.SORT_BY_REGISTRATION_DATE > 0);
        
        // All should be unique
        Set<Integer> sortValues = new HashSet<>();
        sortValues.add(Definitions.SORT_BY_ID);
        sortValues.add(Definitions.SORT_BY_CATEGORY);
        sortValues.add(Definitions.SORT_BY_PAYMENT_METHOD);
        sortValues.add(Definitions.SORT_BY_STORE);
        sortValues.add(Definitions.SORT_BY_CHARGE_DATE);
        sortValues.add(Definitions.SORT_BY_PRICE);
        sortValues.add(Definitions.SORT_BY_REGISTRATION_DATE);
        
        assertEquals(7, sortValues.size());
    }

    @Test
    public void config_DateFormat() {
        assertEquals("dd/MM/yyyy", Config.DATE_FORMAT);
        assertEquals("/", Config.DATE_FORMAT_CHARACTER);
        assertEquals("-", Config.SEPARATOR);
    }
}
