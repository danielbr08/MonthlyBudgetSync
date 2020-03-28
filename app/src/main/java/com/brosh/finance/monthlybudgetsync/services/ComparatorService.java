package com.brosh.finance.monthlybudgetsync.services;

import android.os.Build;

import androidx.annotation.RequiresApi;


import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;

import java.util.ArrayList;
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

    public static Comparator<Transaction> COMPARE_BY_SHOP = new Comparator<Transaction>() {
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

    public static void sort(List<Transaction> transactions, String sortBy, char ascOrDesc) {
        if (sortBy.equals("מזהה")) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_ID);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_ID));
        } else if (sortBy.equals(TextService.getWordCapitalLetter("קטגוריה"))) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_CATEGORY);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_CATEGORY));
        } else if (sortBy.equals(TextService.getSentenceCapitalLetter("א.תשלום", '.'))) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_PAYMENT_METHOD);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_PAYMENT_METHOD));
        } else if (sortBy.equals(TextService.getWordCapitalLetter("חנות"))) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_SHOP);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_SHOP));
        } else if (sortBy.equals(TextService.getSentenceCapitalLetter("ת.עסקה", '.'))) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_TRANSACTION_DATE);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_TRANSACTION_DATE));
        } else if (sortBy.equals(TextService.getWordCapitalLetter("סכום"))) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_PRICE);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_PRICE));
        } else if (sortBy.equals(TextService.getSentenceCapitalLetter("ת.רישום", '.'))) {
            if (ascOrDesc == Config.UP_ARROW)
                Collections.sort(transactions, COMPARE_BY_REGISTRATION_DATE);
            else if (ascOrDesc == Config.DOWN_ARROW)
                Collections.sort(transactions, Collections.reverseOrder(COMPARE_BY_REGISTRATION_DATE));
        }
    }
}
