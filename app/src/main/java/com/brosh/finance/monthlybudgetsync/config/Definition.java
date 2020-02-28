package com.brosh.finance.monthlybudgetsync.config;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.services.Language;

public class Definition {
    public static final String CATEGORIES = "categories";
    public static final String TRANSACTIONS = "Transactions";
    public static final String MONTHLY_BUDGET = "Monthly Budget";
    private static final Language language = new Language(Config.DEFAULT_LANGUAGE);
    public static final String BUDGETS ="Budget";// todo replace name to Budgets
    public static final String MONTHS ="Months";
    public static final String ZERO ="0";
    public static final String DOT =".";
    public static final String ARROW_RIGHT ="->";
    public static final String HEBREW ="HEB";
    public static final String ENGLISH ="EN";
    public static final String CREDIT_CARD =language.creditCardName;
    public static final String dash="-";
    public static final String balance="balance";
    public static final String budget="budget";
    public static final String name="name";




}
