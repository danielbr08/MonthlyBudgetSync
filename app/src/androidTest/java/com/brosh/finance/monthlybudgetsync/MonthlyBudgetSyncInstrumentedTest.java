package com.brosh.finance.monthlybudgetsync;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.*;
import com.brosh.finance.monthlybudgetsync.utils.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Comprehensive instrumented tests for MonthlyBudgetSync app.
 * These tests run on an Android device or emulator.
 * 
 * Test coverage includes:
 * - Application context validation
 * - Model objects serialization/deserialization simulation
 * - Utility classes with Android-specific functionality
 * - Data integrity tests
 * - Edge cases and boundary conditions
 */
@RunWith(AndroidJUnit4.class)
public class MonthlyBudgetSyncInstrumentedTest {

    private Context appContext;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    // ============================================
    // APPLICATION CONTEXT TESTS
    // ============================================

    @Test
    public void useAppContext() {
        assertEquals("com.brosh.finance.monthlybudgetsync", appContext.getPackageName());
    }

    @Test
    public void appContext_NotNull() {
        assertNotNull(appContext);
    }

    // ============================================
    // BUDGET MODEL TESTS
    // ============================================

    @Test
    public void budget_Creation_Complete() {
        Budget budget = new Budget("Groceries", 500, true, "Walmart", 15, 1);
        
        assertEquals("Groceries", budget.getCategoryName());
        assertEquals(500, budget.getValue());
        assertTrue(budget.isConstPayment());
        assertEquals("Walmart", budget.getShop());
        assertEquals(15, budget.getChargeDay());
        assertEquals(1, budget.getCatPriority());
    }

    @Test
    public void budget_NegativeValue_ClampedToZero() {
        Budget budget = new Budget("Test", -100, false, null, 1, 0);
        assertEquals(0, budget.getValue());
    }

    @Test
    public void budget_ChargeDay_ValidRange() {
        Budget budget = new Budget("Test", 100, false, null, 50, 0);
        assertEquals(31, budget.getChargeDay()); // Clamped to 31
        
        budget.setChargeDay(0);
        assertEquals(1, budget.getChargeDay()); // Clamped to 1
    }

    @Test
    public void budget_Equality() {
        Budget b1 = new Budget("Test", 100, false, null, 15, 1);
        Budget b2 = new Budget("Test", 100, false, null, 15, 2);
        assertEquals(b1, b2); // Priority not in equals
    }

    // ============================================
    // CATEGORY MODEL TESTS
    // ============================================

    @Test
    public void category_Creation_Complete() {
        Category category = new Category("cat-1", "Groceries", 500.0, 500);
        
        assertEquals("cat-1", category.getId());
        assertEquals("Groceries", category.getName());
        assertEquals(500.0, category.getBalance(), 0.001);
        assertEquals(500, category.getBudget());
        assertTrue(category.getTransactions().isEmpty());
    }

    @Test
    public void category_TransactionOperations() {
        Category category = new Category("cat-1", "Test", 100.0, 100);
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, 10.0);
        
        category.addTransactions("t1", trans);
        assertTrue(category.hasTransaction("t1"));
        assertEquals(trans, category.getTransaction("t1"));
        assertEquals(1, category.getTransactions().size());
        
        category.removeTransaction("t1");
        assertFalse(category.hasTransaction("t1"));
    }

    @Test
    public void category_BalanceOperations() {
        Category category = new Category("cat-1", "Test", 500.0, 500);
        
        category.withdrawal(100);
        assertEquals(400.0, category.getBalance(), 0.001);
        
        category.deposit(50);
        assertEquals(450.0, category.getBalance(), 0.001);
    }

    @Test
    public void category_ComputedProperties() {
        Category category = new Category("cat-1", "Test", 200.0, 500);
        
        assertEquals(300.0, category.getTotalSpent(), 0.001);
        assertFalse(category.isOverBudget());
        assertEquals(60.0, category.getUsagePercentage(), 0.001);
        
        category.setBalance(-50.0);
        assertTrue(category.isOverBudget());
    }

    // ============================================
    // TRANSACTION MODEL TESTS
    // ============================================

    @Test
    public void transaction_Creation_Complete() {
        Date payDate = new Date();
        Transaction trans = new Transaction("t1", 1, "Groceries", "Cash", "Shop", payDate, 50.0);
        
        assertEquals("t1", trans.getId());
        assertEquals(1, trans.getIdPerMonth());
        assertEquals("Groceries", trans.getCategory());
        assertEquals("Cash", trans.getPaymentMethod());
        assertEquals("Shop", trans.getShop());
        assertEquals(50.0, trans.getPrice(), 0.001);
        assertFalse(trans.isDeleted());
    }

    @Test
    public void transaction_DeleteRestore() {
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, 10.0);
        
        assertTrue(trans.isActive());
        assertFalse(trans.isDeleted());
        
        trans.markDeleted();
        assertFalse(trans.isActive());
        assertTrue(trans.isDeleted());
        
        trans.restore();
        assertTrue(trans.isActive());
        assertFalse(trans.isDeleted());
    }

    @Test
    public void transaction_NegativePrice_ClampedToZero() {
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, -50.0);
        assertEquals(0.0, trans.getPrice(), 0.001);
    }

    // ============================================
    // MONTH MODEL TESTS
    // ============================================

    @Test
    public void month_Creation_CurrentMonth_IsActive() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int monthNum = cal.get(Calendar.MONTH) + 1;
        String yearMonth = year + "-" + (monthNum < 10 ? "0" + monthNum : monthNum);
        
        Month month = new Month(yearMonth, 1L, 15);
        assertTrue(month.isActive());
    }

    @Test
    public void month_Creation_PastMonth_NotActive() {
        Month month = new Month("2020-01", 1L, 15);
        assertFalse(month.isActive());
    }

    @Test
    public void month_CategoryOperations() {
        Month month = new Month("2024-06", 1L, 15);
        Category category = new Category("cat-1", "Test", 100.0, 100);
        
        month.addCategory("cat-1", category);
        assertTrue(month.hasCategory("cat-1"));
        assertEquals(category, month.getCategory("cat-1"));
        assertEquals(1, month.getCategoryCount());
    }

    @Test
    public void month_TransactionIdNumerator() {
        Month month = new Month("2024-06", 1L, 15);
        
        assertEquals(1, month.getTranIdNumerator());
        assertEquals(1, month.getNextTransactionId());
        assertEquals(2, month.getTranIdNumerator());
    }

    // ============================================
    // USER MODEL TESTS
    // ============================================

    @Test
    public void user_Creation_Complete() {
        User user = new User("uid-1", "John", "john@example.com", "+123", "db-key");
        
        assertEquals("uid-1", user.getUid());
        assertEquals("John", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("+123", user.getPhone());
        assertEquals("db-key", user.getDbKey());
    }

    @Test
    public void user_EmailCommaConversion() {
        User user = new User("uid", "Name", "john@example.com", null, "key");
        assertEquals("john@example,com", user.getEmailComma());
    }

    @Test
    public void user_IsOwner() {
        User owner = new User("uid-1", "Name", "email@test.com", null, null);
        // dbKey defaults to uid when null
        assertTrue(owner.isOwner());
        
        User guest = new User("guest-uid", "Name", "email@test.com", null, "owner-db-key");
        assertFalse(guest.isOwner());
    }

    // ============================================
    // VALIDATION UTIL TESTS (Android Context)
    // ============================================

    @Test
    public void validation_Email_Valid() {
        assertTrue(ValidationUtil.isValidEmail("test@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user.name@domain.co.uk"));
    }

    @Test
    public void validation_Email_Invalid() {
        assertFalse(ValidationUtil.isValidEmail("invalid"));
        assertFalse(ValidationUtil.isValidEmail("@example.com"));
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    public void validation_Password_Valid() {
        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertTrue(ValidationUtil.isValidPassword("password123"));
    }

    @Test
    public void validation_Password_Invalid() {
        assertFalse(ValidationUtil.isValidPassword("12345")); // Too short
        assertFalse(ValidationUtil.isValidPassword(null));
    }

    @Test
    public void validation_FirebaseChars() {
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test.value"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test$value"));
        assertTrue(ValidationUtil.containsIllegalFirebaseChars("test#value"));
        assertFalse(ValidationUtil.containsIllegalFirebaseChars("valid-key_123"));
    }

    // ============================================
    // DATE UTIL TESTS (Android Context)
    // ============================================

    @Test
    public void dateUtil_GetTodayDate() {
        Date today = DateUtil.getTodayDate();
        assertNotNull(today);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    @Test
    public void dateUtil_GetCurrentDate() {
        String date = DateUtil.getCurrentDate("/");
        assertNotNull(date);
        assertTrue(date.matches("\\d{2}/\\d{2}/\\d{4}"));
    }

    @Test
    public void dateUtil_GetYearMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 15);
        
        String result = DateUtil.getYearMonth(cal.getTime(), "-");
        assertEquals("2024-06", result);
    }

    @Test
    public void dateUtil_IsSameYearMonth() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2024, Calendar.JUNE, 1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.JUNE, 30);
        
        assertTrue(DateUtil.isSameYearMonth(cal1.getTime(), cal2.getTime()));
    }

    // ============================================
    // FORMAT UTIL TESTS
    // ============================================

    @Test
    public void formatUtil_Currency() {
        String result = FormatUtil.formatCurrency(1234.56, "$");
        assertEquals("$ 1,234.56", result);
    }

    @Test
    public void formatUtil_Decimal() {
        String result = FormatUtil.formatDecimal(1234.5);
        assertEquals("1,234.5", result);
    }

    @Test
    public void formatUtil_Integer() {
        String result = FormatUtil.formatInteger(1234);
        assertEquals("1,234", result);
    }

    @Test
    public void formatUtil_RoundToTwoDecimals() {
        assertEquals(1.24, FormatUtil.roundToTwoDecimals(1.235), 0.001);
        assertEquals(1.23, FormatUtil.roundToTwoDecimals(1.234), 0.001);
    }

    // ============================================
    // TEXT UTIL TESTS (Android Context)
    // ============================================

    @Test
    public void textUtil_CapitalLetter() {
        assertEquals("Hello", TextUtil.getWordCapitalLetter("hello"));
        assertEquals("Hello World", TextUtil.getSentenceCapitalLetter("hello world", ' '));
    }

    @Test
    public void textUtil_EmailComma() {
        assertEquals("test@example,com", TextUtil.getEmailComma("test@example.com"));
        assertEquals("", TextUtil.getEmailComma(null));
    }

    @Test
    public void textUtil_SafeTrim() {
        assertEquals("hello", TextUtil.safeTrim("  hello  "));
        assertEquals("", TextUtil.safeTrim(null));
    }

    @Test
    public void textUtil_IsEmailValid() {
        assertTrue(TextUtil.isEmailValid("test@example.com"));
        assertFalse(TextUtil.isEmailValid("invalid"));
    }

    // ============================================
    // COMPARATOR UTIL TESTS
    // ============================================

    @Test
    public void comparatorUtil_SortById() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("t1", 3, "Cat", "Cash", "Shop", null, 10.0));
        transactions.add(new Transaction("t2", 1, "Cat", "Cash", "Shop", null, 20.0));
        transactions.add(new Transaction("t3", 2, "Cat", "Cash", "Shop", null, 30.0));
        
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_ID, Config.UP_ARROW);
        
        assertEquals(1, transactions.get(0).getIdPerMonth());
        assertEquals(2, transactions.get(1).getIdPerMonth());
        assertEquals(3, transactions.get(2).getIdPerMonth());
    }

    @Test
    public void comparatorUtil_SortByPrice() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("t1", 1, "Cat", "Cash", "Shop", null, 50.0));
        transactions.add(new Transaction("t2", 2, "Cat", "Cash", "Shop", null, 10.0));
        transactions.add(new Transaction("t3", 3, "Cat", "Cash", "Shop", null, 30.0));
        
        ComparatorUtil.sort(transactions, Definitions.SORT_BY_PRICE, Config.UP_ARROW);
        
        assertEquals(10.0, transactions.get(0).getPrice(), 0.001);
        assertEquals(30.0, transactions.get(1).getPrice(), 0.001);
        assertEquals(50.0, transactions.get(2).getPrice(), 0.001);
    }

    // ============================================
    // COMPLEX SCENARIO TESTS
    // ============================================

    @Test
    public void scenario_CreateCompleteMonth() {
        // Create a complete month with categories and transactions
        Month month = new Month("2024-06", 1L, 15);
        
        Category groceries = new Category("cat-1", "Groceries", 500.0, 500);
        Category entertainment = new Category("cat-2", "Entertainment", 200.0, 200);
        
        Transaction t1 = new Transaction("t1", month.getNextTransactionId(), "Groceries", "Cash", "Walmart", new Date(), 50.0);
        Transaction t2 = new Transaction("t2", month.getNextTransactionId(), "Groceries", "Card", "Target", new Date(), 75.0);
        Transaction t3 = new Transaction("t3", month.getNextTransactionId(), "Entertainment", "Cash", "Cinema", new Date(), 25.0);
        
        groceries.addTransactions("t1", t1);
        groceries.addTransactions("t2", t2);
        groceries.withdrawal(125.0); // t1 + t2
        
        entertainment.addTransactions("t3", t3);
        entertainment.withdrawal(25.0);
        
        month.addCategory("cat-1", groceries);
        month.addCategory("cat-2", entertainment);
        
        assertEquals(2, month.getCategoryCount());
        assertEquals(375.0, groceries.getBalance(), 0.001);
        assertEquals(175.0, entertainment.getBalance(), 0.001);
        assertEquals(2, groceries.getTransactions().size());
        assertEquals(1, entertainment.getTransactions().size());
    }

    @Test
    public void scenario_UserSharingBudget() {
        // Test sharing scenario
        User owner = new User("owner-uid", "Owner", "owner@test.com", "+123", null);
        assertTrue(owner.isOwner());
        
        User guest = new User("guest-uid", "Guest", "guest@test.com", "+456", "owner-uid");
        guest.setOwnerUid("owner-uid");
        assertFalse(guest.isOwner());
        assertEquals("owner-uid", guest.getDbKey());
    }

    @Test
    public void scenario_OverBudget() {
        Category category = new Category("cat-1", "Test", 100.0, 100);
        
        assertFalse(category.isOverBudget());
        assertEquals(0.0, category.getTotalSpent(), 0.001);
        
        category.withdrawal(150.0);
        
        assertTrue(category.isOverBudget());
        assertEquals(-50.0, category.getBalance(), 0.001);
        assertEquals(150.0, category.getTotalSpent(), 0.001);
        assertEquals(150.0, category.getUsagePercentage(), 0.001);
    }

    @Test
    public void scenario_DeleteAndRestoreTransaction() {
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, 100.0);
        
        assertTrue(trans.isActive());
        assertFalse(trans.isDeleted());
        
        trans.markDeleted();
        assertFalse(trans.isActive());
        assertTrue(trans.isDeleted());
        
        trans.restore();
        assertTrue(trans.isActive());
        assertFalse(trans.isDeleted());
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    public void edgeCase_EmptyMonth() {
        Month month = new Month("2024-06", 1L, 15);
        assertEquals(0, month.getCategoryCount());
        assertTrue(month.getCategories().isEmpty());
    }

    @Test
    public void edgeCase_ZeroBudgetCategory() {
        Category category = new Category("cat-1", "Test", 0.0, 0);
        assertEquals(0.0, category.getUsagePercentage(), 0.001);
    }

    @Test
    public void edgeCase_UnicodeData() {
        Budget budget = new Budget("מזון", 500, false, "סופר", 15, 1);
        assertEquals("מזון", budget.getCategoryName());
        assertEquals("סופר", budget.getShop());
        
        User user = new User("uid", "יוחנן", "test@test.com", null, null);
        assertEquals("יוחנן", user.getName());
    }

    @Test
    public void edgeCase_LargeNumbers() {
        Budget budget = new Budget("Test", Integer.MAX_VALUE, false, null, 15, 1);
        assertEquals(Integer.MAX_VALUE, budget.getValue());
        
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, trans.getPrice(), 0.001);
    }

    @Test
    public void edgeCase_NullHandling() {
        // Budget with nulls
        Budget budget = new Budget(null, 100, false, null, 15, 1);
        assertNull(budget.getCategoryName());
        assertNull(budget.getShop());
        
        // Category with nulls
        Category category = new Category(null, null, 100.0, 100);
        assertNull(category.getId());
        assertNull(category.getName());
        
        // User with nulls
        User user = new User(null, null, null, null, null);
        assertNull(user.getUid());
        assertNull(user.getName());
    }

    // ============================================
    // CONFIG AND DEFINITIONS TESTS
    // ============================================

    @Test
    public void config_DateFormat() {
        assertEquals("dd/MM/yyyy", Config.DATE_FORMAT);
        assertEquals("/", Config.DATE_FORMAT_CHARACTER);
        assertEquals("-", Config.SEPARATOR);
    }

    @Test
    public void definitions_SortConstants() {
        assertEquals(1, Definitions.SORT_BY_ID);
        assertEquals(2, Definitions.SORT_BY_CATEGORY);
        assertEquals(3, Definitions.SORT_BY_PAYMENT_METHOD);
        assertEquals(4, Definitions.SORT_BY_STORE);
        assertEquals(5, Definitions.SORT_BY_CHARGE_DATE);
        assertEquals(6, Definitions.SORT_BY_PRICE);
        assertEquals(7, Definitions.SORT_BY_REGISTRATION_DATE);
    }

    @Test
    public void definitions_UpdateTypeConstants() {
        assertEquals(1, Definitions.UPDATE_EMAIL);
        assertEquals(2, Definitions.UPDATE_PASSWORD);
        assertEquals(3, Definitions.UPDATE_PHONE_NUMBER);
        assertEquals(4, Definitions.UPDATE_USER_NAME);
    }
}
