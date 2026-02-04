package com.brosh.finance.monthlybudgetsync.utils;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ComparatorUtil {
    
    private ComparatorUtil() {
        // Utility class
    }
    
    public static final Comparator<Transaction> COMPARE_BY_ID = (one, other) ->
            Integer.compare(one.getIdPerMonth(), other.getIdPerMonth());

    public static final Comparator<Transaction> COMPARE_BY_PAYMENT_METHOD = (one, other) ->
            one.getPaymentMethod().compareToIgnoreCase(other.getPaymentMethod());

    public static final Comparator<Transaction> COMPARE_BY_CATEGORY = (one, other) ->
            one.getCategory().compareToIgnoreCase(other.getCategory());

    public static final Comparator<Transaction> COMPARE_BY_STORE = (one, other) ->
            one.getShop().compareToIgnoreCase(other.getShop());

    public static final Comparator<Transaction> COMPARE_BY_TRANSACTION_DATE = (one, other) ->
            one.getPayDate().compareTo(other.getPayDate());

    public static final Comparator<Transaction> COMPARE_BY_PRICE = (one, other) ->
            Double.compare(one.getPrice(), other.getPrice());

    public static final Comparator<Transaction> COMPARE_BY_REGISTRATION_DATE = (one, other) ->
            one.getRegistrationDate().compareTo(other.getPayDate());

    @SuppressWarnings("unused") // May be used for future functionality
    public static final Comparator<String> COMPARE_STRING = String::compareToIgnoreCase;

    public static final Comparator<Budget> COMPARE_BY_CATEGORY_PRIORITY = (one, other) ->
            Long.compare(one.getCatPriority(), other.getCatPriority());

    public static void sort(List<Transaction> transactions, int sortBy, char ascOrDesc) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        
        if (sortBy == Definitions.SORT_BY_ID) {
            if (ascOrDesc == Config.UP_ARROW) {
                transactions.sort(COMPARE_BY_ID);
            } else if (ascOrDesc == Config.DOWN_ARROW) {
                transactions.sort(Collections.reverseOrder(COMPARE_BY_ID));
            }
        } else if (sortBy == Definitions.SORT_BY_CATEGORY) {
            if (ascOrDesc == Config.UP_ARROW) {
                transactions.sort(COMPARE_BY_CATEGORY);
            } else if (ascOrDesc == Config.DOWN_ARROW) {
                transactions.sort(Collections.reverseOrder(COMPARE_BY_CATEGORY));
            }
        } else if (sortBy == Definitions.SORT_BY_PAYMENT_METHOD) {
            if (ascOrDesc == Config.UP_ARROW) {
                transactions.sort(COMPARE_BY_PAYMENT_METHOD);
            } else if (ascOrDesc == Config.DOWN_ARROW) {
                transactions.sort(Collections.reverseOrder(COMPARE_BY_PAYMENT_METHOD));
            }
        } else if (sortBy == Definitions.SORT_BY_STORE) {
            if (ascOrDesc == Config.UP_ARROW) {
                transactions.sort(COMPARE_BY_STORE);
            } else if (ascOrDesc == Config.DOWN_ARROW) {
                transactions.sort(Collections.reverseOrder(COMPARE_BY_STORE));
            }
        } else if (sortBy == Definitions.SORT_BY_CHARGE_DATE) {
            if (ascOrDesc == Config.UP_ARROW) {
                transactions.sort(COMPARE_BY_TRANSACTION_DATE);
            } else if (ascOrDesc == Config.DOWN_ARROW) {
                transactions.sort(Collections.reverseOrder(COMPARE_BY_TRANSACTION_DATE));
            }
        } else if (sortBy == Definitions.SORT_BY_PRICE) {
            if (ascOrDesc == Config.UP_ARROW) {
                transactions.sort(COMPARE_BY_PRICE);
            } else if (ascOrDesc == Config.DOWN_ARROW) {
                transactions.sort(Collections.reverseOrder(COMPARE_BY_PRICE));
            }
        } else if (sortBy == Definitions.SORT_BY_REGISTRATION_DATE) {
            if (ascOrDesc == Config.UP_ARROW) {
                transactions.sort(COMPARE_BY_REGISTRATION_DATE);
            } else if (ascOrDesc == Config.DOWN_ARROW) {
                transactions.sort(Collections.reverseOrder(COMPARE_BY_REGISTRATION_DATE));
            }
        }
    }
}
