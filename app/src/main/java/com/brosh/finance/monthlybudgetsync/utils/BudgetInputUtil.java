package com.brosh.finance.monthlybudgetsync.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Budget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure Java helpers for parsing and validating budget template rows.
 * Used by CreateBudgetActivity and unit tests.
 */
public final class BudgetInputUtil {

    public enum RowError {
        NONE,
        DUPLICATE_CATEGORY,
        ILLEGAL_CATEGORY_CHAR,
        EMPTY_CATEGORY,
        ZERO_VALUE,
        EMPTY_SHOP,
        ILLEGAL_SHOP_CHAR
    }

    private BudgetInputUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Parses a budget amount from user input. Accepts integers and decimals (rounded).
     */
    public static int parseBudgetAmount(@Nullable String raw) {
        if (ValidationUtil.isEmpty(raw)) {
            return 0;
        }
        String cleaned = raw.trim().replace(Definitions.COMMA, "");
        int value;
        if (cleaned.contains(".") || cleaned.contains("e") || cleaned.contains("E")) {
            value = (int) Math.round(ValidationUtil.parseDouble(cleaned, 0));
        } else {
            value = ValidationUtil.parseInt(cleaned, 0);
        }
        return Math.max(0, value);
    }

    /**
     * Parses charge day from user input (1–31 after clamping in {@link Budget}).
     */
    public static int parseChargeDay(@Nullable String raw) {
        int day = ValidationUtil.parseInt(ValidationUtil.safeTrim(raw), 1);
        return Math.max(1, Math.min(31, day));
    }

    /**
     * Validates a single budget row. {@code categoriesSoFar} must already include
     * the current category name (same behavior as CreateBudgetActivity).
     */
    @NonNull
    public static RowError validateRow(
            @NonNull String category,
            int value,
            boolean constPayment,
            @Nullable String shop,
            @NonNull List<String> categoriesSoFar) {

        String categoryTrimmed = ValidationUtil.safeTrim(category);
        if (ValidationUtil.isEmpty(categoryTrimmed)) {
            return RowError.EMPTY_CATEGORY;
        }
        if (Collections.frequency(categoriesSoFar, category) > 1) {
            return RowError.DUPLICATE_CATEGORY;
        }
        if (ValidationUtil.containsSeparator(categoryTrimmed, TextUtil.getSeparator())
                || ValidationUtil.containsIllegalFirebaseChars(categoryTrimmed)) {
            return RowError.ILLEGAL_CATEGORY_CHAR;
        }
        if (value == 0) {
            return RowError.ZERO_VALUE;
        }
        String shopTrimmed = ValidationUtil.safeTrim(shop);
        if (constPayment && shopTrimmed.isEmpty()) {
            return RowError.EMPTY_SHOP;
        }
        if (ValidationUtil.containsSeparator(shopTrimmed, TextUtil.getSeparator())
                || ValidationUtil.containsIllegalFirebaseChars(shopTrimmed)) {
            return RowError.ILLEGAL_SHOP_CHAR;
        }
        return RowError.NONE;
    }

    /**
     * Builds a {@link Budget} from parsed row fields.
     */
    @NonNull
    public static Budget buildBudget(
            @NonNull String category,
            int value,
            boolean constPayment,
            @Nullable String shop,
            int chargeDay,
            int priority) {
        String shopValue = constPayment ? ValidationUtil.safeTrim(shop) : null;
        return new Budget(category, value, constPayment, shopValue, chargeDay, priority);
    }

    /**
     * Returns true if any budget in {@code current} differs from {@code original} by content or count.
     */
    public static boolean hasBudgetChanges(@NonNull List<Budget> original, @NonNull List<Budget> current) {
        for (Budget bgt : current) {
            if (!budgetExistsIn(bgt, original)) {
                return true;
            }
        }
        return current.size() != original.size();
    }

    /**
     * Returns true if existing categories were modified or removed (not only additions).
     */
    public static boolean hasOriginContentChanged(@NonNull List<Budget> original, @NonNull List<Budget> current) {
        int matchCount = 0;
        for (Budget oldBgt : original) {
            if (budgetExistsIn(oldBgt, current)) {
                matchCount++;
            }
        }
        return matchCount != original.size();
    }

    /**
     * Budgets present in {@code current} but not in {@code original}.
     */
    @NonNull
    public static List<Budget> getAddedBudgets(@NonNull List<Budget> original, @NonNull List<Budget> current) {
        List<Budget> added = new ArrayList<>();
        for (Budget bgt : current) {
            if (!budgetExistsIn(bgt, original)) {
                added.add(bgt);
            }
        }
        return added;
    }

    private static boolean budgetExistsIn(@NonNull Budget budget, @NonNull List<Budget> list) {
        for (Budget item : list) {
            if (budget.equals(item)) {
                return true;
            }
        }
        return false;
    }
}
