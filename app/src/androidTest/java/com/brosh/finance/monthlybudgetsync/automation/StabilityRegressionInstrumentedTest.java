package com.brosh.finance.monthlybudgetsync.automation;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.utils.BudgetInputUtil;
import com.brosh.finance.monthlybudgetsync.utils.TextUtil;
import com.brosh.finance.monthlybudgetsync.utils.ValidationUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;

/**
 * On-device regression tests for stability-sensitive parsing and validation paths.
 */
@RunWith(AndroidJUnit4.class)
public class StabilityRegressionInstrumentedTest {

    @Test
    public void budgetAmountParsing_handlesDecimalsAndCommas() {
        assertEquals(13, BudgetInputUtil.parseBudgetAmount("12.6"));
        assertEquals(1234, BudgetInputUtil.parseBudgetAmount("1,234"));
        assertEquals(0, BudgetInputUtil.parseBudgetAmount("not-a-number"));
    }

    @Test
    public void budgetRowValidation_rejectsDuplicateCategories() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Food");
        categories.add("Food");
        assertEquals(BudgetInputUtil.RowError.DUPLICATE_CATEGORY,
                BudgetInputUtil.validateRow("Food", 100, false, null, categories));
    }

    @Test
    public void budgetRowValidation_rejectsIllegalSeparatorInCategory() {
        String separator = TextUtil.getSeparator();
        ArrayList<String> categories = new ArrayList<>(
                Collections.singletonList("Bad" + separator + "Name"));
        assertEquals(BudgetInputUtil.RowError.ILLEGAL_CATEGORY_CHAR,
                BudgetInputUtil.validateRow("Bad" + separator + "Name", 50, false, null, categories));
    }

    @Test
    public void chargeDayParsing_clampsToValidRange() {
        assertEquals(1, BudgetInputUtil.parseChargeDay("0"));
        assertEquals(31, BudgetInputUtil.parseChargeDay("99"));
        assertEquals(15, BudgetInputUtil.parseChargeDay("15"));
    }

    @Test
    public void transactionPriceParsing_neverThrowsOnGarbageInput() {
        assertEquals(0.0, ValidationUtil.parseDouble("abc", 0), 0.001);
        assertEquals(12.5, ValidationUtil.parseDouble("12.5", 0), 0.001);
    }

    @Test
    public void buildBudget_createsValidObject() {
        Budget budget = BudgetInputUtil.buildBudget("Rent", 1200, true, "Landlord", 5, 1);
        assertEquals("Rent", budget.getCategoryName());
        assertEquals(1200, budget.getValue());
        assertTrue(budget.isConstPayment());
        assertEquals("Landlord", budget.getShop());
    }
}
