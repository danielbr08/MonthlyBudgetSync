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

import java.util.*;

import static org.junit.Assert.*;

/**
 * Production edge case tests that require Android context.
 * 
 * Covers:
 * - Email validation with Android Patterns
 * - Network connectivity scenarios
 * - Context-aware operations
 * - Real-world production scenarios
 */
@RunWith(AndroidJUnit4.class)
public class ProductionEdgeCasesInstrumentedTest {

    private Context appContext;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    // ============================================
    // EMAIL VALIDATION (Requires Android Patterns)
    // ============================================

    @Test
    public void emailValidation_Standard() {
        assertTrue(ValidationUtil.isValidEmail("test@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user@domain.org"));
    }

    @Test
    public void emailValidation_Complex() {
        assertTrue(ValidationUtil.isValidEmail("user.name@subdomain.domain.co.uk"));
        assertTrue(ValidationUtil.isValidEmail("user+tag@gmail.com"));
        assertTrue(ValidationUtil.isValidEmail("name.surname@company.com"));
    }

    @Test
    public void emailValidation_Invalid() {
        assertFalse(ValidationUtil.isValidEmail("invalid"));
        assertFalse(ValidationUtil.isValidEmail("@nodomain.com"));
        assertFalse(ValidationUtil.isValidEmail("noat.domain.com"));
        assertFalse(ValidationUtil.isValidEmail("spaces in@email.com"));
    }

    @Test
    public void emailValidation_EdgeCases() {
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail("   "));
    }

    @Test
    public void emailValidation_International() {
        // May or may not be valid depending on Android version
        // Test just ensures no crash
        try {
            ValidationUtil.isValidEmail("用户@例子.广告");
        } catch (Exception e) {
            fail("Should not throw exception for international email");
        }
    }

    // ============================================
    // DATE PARSING EDGE CASES (with Log.w)
    // ============================================

    @Test
    public void dateParsing_InvalidFormat() {
        Date result = DateUtil.convertStringToDate("not-a-date", Config.DATE_FORMAT);
        assertNull(result);
    }

    @Test
    public void dateParsing_WrongFormat() {
        Date result = DateUtil.convertStringToDate("2024-01-15", Config.DATE_FORMAT);
        assertNull(result);
    }

    @Test
    public void dateParsing_InvalidDay() {
        Date result = DateUtil.convertStringToDate("32/01/2024", Config.DATE_FORMAT);
        assertNull(result);
    }

    @Test
    public void dateParsing_InvalidMonth() {
        Date result = DateUtil.convertStringToDate("15/13/2024", Config.DATE_FORMAT);
        assertNull(result);
    }

    @Test
    public void dateParsing_PartialDate() {
        Date result = DateUtil.convertStringToDate("15/01", Config.DATE_FORMAT);
        assertNull(result);
    }

    // ============================================
    // REAL-WORLD PRODUCTION SCENARIOS
    // ============================================

    @Test
    public void scenario_NewUserFirstBudget() {
        // Simulate new user creating their first budget
        User user = new User("new-user-uid", "New User", "newuser@test.com", null, null);
        assertTrue(user.isOwner());
        
        // Create first month
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int monthNum = cal.get(Calendar.MONTH) + 1;
        String yearMonth = year + "-" + (monthNum < 10 ? "0" + monthNum : monthNum);
        
        Month month = new Month(yearMonth, 1L, cal.get(Calendar.DAY_OF_MONTH));
        assertTrue(month.isActive());
        assertEquals(0, month.getCategoryCount());
    }

    @Test
    public void scenario_AddingFirstTransaction() {
        Category category = new Category("cat-1", "Groceries", 500.0, 500);
        Month month = new Month("2024-06", 1L, 15);
        
        // First transaction
        int tranId = month.getNextTransactionId();
        Transaction trans = new Transaction(
            "trans-" + tranId,
            tranId,
            "Groceries",
            "Cash",
            "Walmart",
            DateUtil.getTodayDate(),
            45.67
        );
        
        category.addTransactions(trans.getId(), trans);
        category.withdrawal(trans.getPrice());
        month.addCategory("cat-1", category);
        
        assertEquals(1, category.getTransactions().size());
        assertEquals(454.33, category.getBalance(), 0.01);
    }

    @Test
    public void scenario_MonthRollover() {
        // Current month
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int monthNum = cal.get(Calendar.MONTH) + 1;
        String currentYearMonth = year + "-" + (monthNum < 10 ? "0" + monthNum : monthNum);
        
        Month currentMonth = new Month(currentYearMonth, 1L, 15);
        assertTrue(currentMonth.isActive());
        
        // Next month
        cal.add(Calendar.MONTH, 1);
        year = cal.get(Calendar.YEAR);
        monthNum = cal.get(Calendar.MONTH) + 1;
        String nextYearMonth = year + "-" + (monthNum < 10 ? "0" + monthNum : monthNum);
        
        Month nextMonth = new Month(nextYearMonth, 1L, 15);
        assertFalse(nextMonth.isActive()); // Future month not active
    }

    @Test
    public void scenario_SharingBudgetWithPartner() {
        // Owner creates budget
        User owner = new User("owner-uid", "Owner", "owner@test.com", "+1234567890", null);
        assertTrue(owner.isOwner());
        
        // Partner accepts share
        User partner = new User("partner-uid", "Partner", "partner@test.com", "+0987654321", "owner-uid");
        partner.setOwnerUid("owner-uid");
        assertFalse(partner.isOwner());
        assertEquals("owner-uid", partner.getDbKey());
        
        // Both access same budget
        assertEquals(owner.getDbKey(), partner.getDbKey());
    }

    @Test
    public void scenario_DeleteAndRestoreTransaction() {
        Category category = new Category("cat-1", "Test", 100.0, 100);
        Transaction trans = new Transaction("t1", 1, "Test", "Cash", "Shop", null, 50.0);
        
        // Add and withdraw
        category.addTransactions("t1", trans);
        category.withdrawal(50.0);
        assertEquals(50.0, category.getBalance(), 0.001);
        
        // Delete (soft delete) - should restore balance
        trans.markDeleted();
        category.deposit(50.0);
        assertEquals(100.0, category.getBalance(), 0.001);
        assertTrue(trans.isDeleted());
        
        // Restore - should withdraw again
        trans.restore();
        category.withdrawal(50.0);
        assertEquals(50.0, category.getBalance(), 0.001);
        assertFalse(trans.isDeleted());
    }

    @Test
    public void scenario_OverBudgetWarning() {
        Category category = new Category("cat-1", "Groceries", 100.0, 100);
        
        // Spend within budget
        category.withdrawal(80.0);
        assertFalse(category.isOverBudget());
        assertEquals(80.0, category.getUsagePercentage(), 0.001);
        
        // Go over budget
        category.withdrawal(50.0);
        assertTrue(category.isOverBudget());
        assertEquals(130.0, category.getUsagePercentage(), 0.001);
        assertEquals(-30.0, category.getBalance(), 0.001);
    }

    @Test
    public void scenario_MultiplePaymentMethods() {
        Category category = new Category("cat-1", "Groceries", 500.0, 500);
        
        Transaction t1 = new Transaction("t1", 1, "Groceries", "Cash", "Shop1", null, 50.0);
        Transaction t2 = new Transaction("t2", 2, "Groceries", "Credit Card", "Shop2", null, 100.0);
        Transaction t3 = new Transaction("t3", 3, "Groceries", "Debit Card", "Shop3", null, 75.0);
        Transaction t4 = new Transaction("t4", 4, "Groceries", "Cash", "Shop4", null, 25.0);
        
        category.addTransactions("t1", t1);
        category.addTransactions("t2", t2);
        category.addTransactions("t3", t3);
        category.addTransactions("t4", t4);
        
        assertEquals(4, category.getTransactions().size());
        
        // Sort by payment method
        List<Transaction> transList = new ArrayList<>(category.getTransactions().values());
        ComparatorUtil.sort(transList, Definitions.SORT_BY_PAYMENT_METHOD, Config.UP_ARROW);
        
        assertEquals("Cash", transList.get(0).getPaymentMethod());
    }

    @Test
    public void scenario_ConstantPaymentBudget() {
        // Constant payment (recurring)
        Budget constantBudget = new Budget("Netflix", 15, true, "Netflix", 1, 1);
        assertTrue(constantBudget.isConstPayment());
        assertEquals("Netflix", constantBudget.getShop());
        assertEquals(1, constantBudget.getChargeDay());
        
        // Variable payment
        Budget variableBudget = new Budget("Groceries", 500, false, null, 15, 2);
        assertFalse(variableBudget.isConstPayment());
        assertNull(variableBudget.getShop());
    }

    // ============================================
    // CURRENCY AND FORMATTING SCENARIOS
    // ============================================

    @Test
    public void currency_HebrewShekel() {
        String formatted = FormatUtil.formatCurrency(1234.56, "₪");
        assertTrue(formatted.contains("₪"));
        assertTrue(formatted.contains("1,234"));
    }

    @Test
    public void currency_Dollar() {
        String formatted = FormatUtil.formatCurrency(1234.56, "$");
        assertEquals("$ 1,234.56", formatted);
    }

    @Test
    public void currency_Euro() {
        String formatted = FormatUtil.formatCurrency(1234.56, "€");
        assertTrue(formatted.contains("€"));
    }

    @Test
    public void currency_NoCurrency() {
        String formatted = FormatUtil.formatCurrency(1234.56, null);
        assertEquals("1,234.56", formatted);
    }

    // ============================================
    // LARGE DATA PRODUCTION SCENARIOS
    // ============================================

    @Test
    public void production_MonthWithManyTransactions() {
        Category category = new Category("cat-1", "Groceries", 10000.0, 10000);
        Month month = new Month("2024-06", 1L, 15);
        
        // Add 365 transactions (like daily spending for a year)
        for (int i = 0; i < 365; i++) {
            int tranId = month.getNextTransactionId();
            Transaction trans = new Transaction(
                "trans-" + tranId,
                tranId,
                "Groceries",
                i % 2 == 0 ? "Cash" : "Card",
                "Shop " + (i % 50),
                null,
                10.0 + (i % 100)
            );
            category.addTransactions(trans.getId(), trans);
            category.withdrawal(trans.getPrice());
        }
        
        assertEquals(365, category.getTransactions().size());
        
        // Verify sorting still works
        List<Transaction> transList = new ArrayList<>(category.getTransactions().values());
        ComparatorUtil.sort(transList, Definitions.SORT_BY_PRICE, Config.DOWN_ARROW);
        
        assertTrue(transList.get(0).getPrice() >= transList.get(364).getPrice());
    }

    @Test
    public void production_YearOfMonths() {
        List<Month> months = new ArrayList<>();
        
        // Create 12 months
        for (int m = 1; m <= 12; m++) {
            String yearMonth = "2024-" + (m < 10 ? "0" + m : m);
            Month month = new Month(yearMonth, 1L, 15);
            
            // Add categories
            for (int c = 0; c < 10; c++) {
                Category cat = new Category("cat-" + c, "Category " + c, 1000.0, 1000);
                month.addCategory("cat-" + c, cat);
            }
            
            months.add(month);
        }
        
        assertEquals(12, months.size());
        
        // All months should have same structure
        for (Month month : months) {
            assertEquals(10, month.getCategoryCount());
        }
    }

    // ============================================
    // ERROR RECOVERY SCENARIOS
    // ============================================

    @Test
    public void recovery_NullCategoryInMonth() {
        Month month = new Month("2024-06", 1L, 15);
        
        // Attempt to add null category
        month.addCategory("null-cat", null);
        
        // Should not crash
        assertFalse(month.hasCategory("null-cat"));
    }

    @Test
    public void recovery_NullTransactionInCategory() {
        Category category = new Category("cat-1", "Test", 100.0, 100);
        
        // Attempt to add null transaction
        category.addTransactions("null-trans", null);
        
        // Should not crash, map might have null value
        // Just verify no exception
        assertTrue(true);
    }

    @Test
    public void recovery_EmptyStringsEverywhere() {
        Budget budget = new Budget("", 100, false, "", 15, 0);
        assertEquals("", budget.getCategoryName());
        assertEquals("", budget.getShop());
        
        Category category = new Category("", "", 100.0, 100);
        assertEquals("", category.getId());
        assertEquals("", category.getName());
        
        User user = new User("", "", "", "", "");
        assertEquals("", user.getUid());
        assertEquals("", user.getName());
        assertEquals("", user.getEmail());
    }

    // ============================================
    // TEXT UTILITY EDGE CASES
    // ============================================

    @Test
    public void textUtil_IsEmailValid_Android() {
        // Uses android.util.Patterns
        assertTrue(TextUtil.isEmailValid("test@example.com"));
        assertFalse(TextUtil.isEmailValid("invalid"));
        assertFalse(TextUtil.isEmailValid(null));
    }

    @Test
    public void textUtil_CapitalizationVariants() {
        assertEquals("Hello", TextUtil.getWordCapitalLetter("hello"));
        assertEquals("Hello", TextUtil.getWordCapitalLetter("Hello"));
        assertEquals("HELLO", TextUtil.getWordCapitalLetter("HELLO"));
        assertEquals("", TextUtil.getWordCapitalLetter(""));
        assertEquals("", TextUtil.getWordCapitalLetter(null));
    }

    @Test
    public void textUtil_SentenceCapitalization() {
        assertEquals("Hello World", TextUtil.getSentenceCapitalLetter("hello world", ' '));
        assertEquals("Hello-World-Test", TextUtil.getSentenceCapitalLetter("hello-world-test", '-'));
    }

    // ============================================
    // LOCALE-SPECIFIC TESTS
    // ============================================

    @Test
    public void locale_HebrewCategoryNames() {
        Budget budget = new Budget("מזון", 500, false, "סופר", 15, 1);
        assertEquals("מזון", budget.getCategoryName());
        assertEquals("סופר", budget.getShop());
        
        Category category = new Category("cat-1", "קטגוריה בעברית", 100.0, 100);
        assertEquals("קטגוריה בעברית", category.getName());
    }

    @Test
    public void locale_RtlText() {
        Transaction trans = new Transaction(
            "t1", 1, 
            "קניות", // Category in Hebrew
            "מזומן", // Payment method in Hebrew
            "חנות בעברית", // Shop in Hebrew
            null, 
            100.0
        );
        trans.setComment("הערה בעברית");
        
        assertEquals("קניות", trans.getCategory());
        assertEquals("מזומן", trans.getPaymentMethod());
        assertEquals("חנות בעברית", trans.getShop());
        assertEquals("הערה בעברית", trans.getComment());
    }
}
