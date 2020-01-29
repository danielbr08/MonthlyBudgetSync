package com.brosh.finance.monthlybudgetsync.services;

import com.brosh.finance.monthlybudgetsync.Config;
import com.brosh.finance.monthlybudgetsync.Language;

public class Definition {
    private static final Language language = new Language(Config.DEFAULT_LANGUAGE);
    public static final String BUDGET ="Budget";
    public static final String MONTHS ="Months";
    public static final String ZERO ="0";
    public static final String DOT =".";
    public static final String ARROW_RIGHT ="->";
    public static final String HEBREW ="HEB";
    public static final String ENGLISH ="EN";
    public static final String CREDIT_CARD =language.creditCardName;
}
