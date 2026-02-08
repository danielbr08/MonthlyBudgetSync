package com.brosh.finance.monthlybudgetsync.utils;

import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Utility class for sorting transactions and budgets.
 * Provides null-safe comparators for all sortable fields.
 */
public final class ComparatorUtil {
    
    private ComparatorUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    // ============================================
    // TRANSACTION COMPARATORS (null-safe)
    // ============================================
    
    /** Compare transactions by ID (ascending) */
    public static final Comparator<Transaction> COMPARE_BY_ID = 
        Comparator.comparingInt(Transaction::getIdPerMonth);

    /** Compare transactions by payment method (case-insensitive, null-safe) */
    public static final Comparator<Transaction> COMPARE_BY_PAYMENT_METHOD = 
        Comparator.comparing(
            Transaction::getPaymentMethod, 
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );

    /** Compare transactions by category (case-insensitive, null-safe) */
    public static final Comparator<Transaction> COMPARE_BY_CATEGORY = 
        Comparator.comparing(
            Transaction::getCategory, 
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );

    /** Compare transactions by store/shop (case-insensitive, null-safe) */
    public static final Comparator<Transaction> COMPARE_BY_STORE = 
        Comparator.comparing(
            Transaction::getShop, 
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );

    /** Compare transactions by pay date (null-safe) */
    public static final Comparator<Transaction> COMPARE_BY_TRANSACTION_DATE = 
        Comparator.comparing(
            Transaction::getPayDate, 
            Comparator.nullsLast(Comparator.naturalOrder())
        );

    /** Compare transactions by price */
    public static final Comparator<Transaction> COMPARE_BY_PRICE = 
        Comparator.comparingDouble(Transaction::getPrice);

    /** Compare transactions by registration date (null-safe) */
    public static final Comparator<Transaction> COMPARE_BY_REGISTRATION_DATE = 
        Comparator.comparing(
            Transaction::getRegistrationDate, 
            Comparator.nullsLast(Comparator.naturalOrder())
        );

    // ============================================
    // BUDGET COMPARATORS
    // ============================================
    
    /** Compare budgets by category priority */
    public static final Comparator<Budget> COMPARE_BY_CATEGORY_PRIORITY = 
        Comparator.comparingInt(Budget::getCatPriority);

    /** Compare strings (case-insensitive, null-safe) */
    public static final Comparator<String> COMPARE_STRING = 
        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);

    // ============================================
    // SORTING METHODS
    // ============================================
    
    /**
     * Sorts transactions by the specified field and direction.
     * 
     * @param transactions the list to sort (modified in-place)
     * @param sortBy the sort field constant from Definitions
     * @param ascOrDesc UP_ARROW for ascending, DOWN_ARROW for descending
     */
    public static void sort(@Nullable List<Transaction> transactions, int sortBy, char ascOrDesc) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        
        Comparator<Transaction> comparator = getComparator(sortBy);
        if (comparator == null) {
            return;
        }
        
        // Apply sort direction
        if (ascOrDesc == Config.DOWN_ARROW) {
            comparator = comparator.reversed();
        }
        
        transactions.sort(comparator);
    }
    
    /**
     * Returns the appropriate comparator for the given sort type.
     * 
     * @param sortBy the sort field constant from Definitions
     * @return the comparator, or null if sortBy is invalid
     */
    @Nullable
    private static Comparator<Transaction> getComparator(int sortBy) {
        return switch (sortBy) {
            case Definitions.SORT_BY_ID -> COMPARE_BY_ID;
            case Definitions.SORT_BY_CATEGORY -> COMPARE_BY_CATEGORY;
            case Definitions.SORT_BY_PAYMENT_METHOD -> COMPARE_BY_PAYMENT_METHOD;
            case Definitions.SORT_BY_STORE -> COMPARE_BY_STORE;
            case Definitions.SORT_BY_CHARGE_DATE -> COMPARE_BY_TRANSACTION_DATE;
            case Definitions.SORT_BY_PRICE -> COMPARE_BY_PRICE;
            case Definitions.SORT_BY_REGISTRATION_DATE -> COMPARE_BY_REGISTRATION_DATE;
            default -> COMPARE_BY_ID; // Default fallback
        };
    }
    
    /**
     * Sorts budgets by category priority.
     * 
     * @param budgets the list to sort
     * @param ascending true for ascending, false for descending
     */
    public static void sortBudgets(@Nullable List<Budget> budgets, boolean ascending) {
        if (budgets == null || budgets.isEmpty()) {
            return;
        }
        
        Comparator<Budget> comparator = COMPARE_BY_CATEGORY_PRIORITY;
        if (!ascending) {
            comparator = comparator.reversed();
        }
        
        budgets.sort(comparator);
    }
}
