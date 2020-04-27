package com.brosh.finance.monthlybudgetsync.services;

import android.os.Build;

import androidx.annotation.RequiresApi;


import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ComparatorService {
    public static Comparator<Transaction> COMPARE_BY_ID = new Comparator<Transaction>() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public int compare(Transaction one, Transaction other) {
            return Integer.compare(one.getIdPerMonth(), other.getIdPerMonth());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_PAYMENT_METHOD = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getPaymentMethod().compareToIgnoreCase(other.getPaymentMethod());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_CATEGORY = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getCategory().compareToIgnoreCase(other.getCategory());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_STORE = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getShop().compareToIgnoreCase(other.getShop());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_TRANSACTION_DATE = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getPayDate().compareTo(other.getPayDate());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_PRICE = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return Double.compare(one.getPrice(), other.getPrice());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_REGISTRATION_DATE = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getRegistrationDate().compareTo(other.getPayDate());
        }
    };

    public static Comparator<String> COMPARE_String = new Comparator<String>() {
        public int compare(String one, String other) {
            return one.compareToIgnoreCase(other);
        }
    };

    public static Comparator<Budget> COMPARE_BY_CATEGORY_PRIORITY = new Comparator<Budget>() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public int compare(Budget one, Budget other) {
            return Long.compare(one.getCatPriority(), other.getCatPriority());
        }
    };

    public static void sort(List<Transaction> transactions, int sortBy, char ascOrDesc) {
        if (sortBy == Definition.SORT_BY_ID) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_ID);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_ID));
        } else if (sortBy == Definition.SORT_BY_CATEGORY) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_CATEGORY);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_CATEGORY));
        } else if (sortBy == Definition.SORT_BY_PAYMRNT_METHOD) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_PAYMENT_METHOD);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_PAYMENT_METHOD));
        } else if (sortBy == Definition.SORT_BY_STORE) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_STORE);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_STORE));
        } else if (sortBy == Definition.SORT_BY_CHARGE_DATE) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_TRANSACTION_DATE);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_TRANSACTION_DATE));
        } else if (sortBy == Definition.SORT_BY_PRICE) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_PRICE);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_PRICE));
        } else if (sortBy == Definition.SORT_BY_REGISTRATION_DATE) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_REGISTRATION_DATE);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_REGISTRATION_DATE));
        }
    }
}
