package com.brosh.finance.monthlybudgetsync;

import com.brosh.finance.monthlybudgetsync.services.Definition;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public final class Config {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String SEPARATOR = "-->";
    public static final char DOWN_ARROW = 'ꜜ';
    public static final char UP_ARROW = 'ꜛ';
    public static final String DEFAULT_LANGUAGE=Definition.HEBREW;
    public static double CATEGORY_NAME_ET_WIDTH_PERCENT = 27/100;
    public static double CATEGORY_VALUE_ET_WIDTH_PERCENT = 14/100;
    public static double CONST_PAYMENT_CB_WIDTH_PERCENT = 14/100;
    public static double SHOP_ET_WIDTH_PERCENT = 23/100;
    public static double OPTIONAL_DAYS_SPINNER_WIDTH_PERCENT = 22/100;

    public static double CATEGORY_NAME_TV_TITLE_WIDTH_PERCENT = 27/100;
    public static double CATEGORY_VALUE_TV_TITLE_WIDTH_PERCENT = 17/100;
    public static double CONST_PAYMENT_TV_TITLE_WIDTH_PERCENT = 12/100;
    public static double SHOP_TV_TITLE_WIDTH_PERCENT = 22/100;
    public static double PAY_DATE_TITLE_WIDTH_PERCENT = 22/100;

    public static Set<String> shopsSet = new TreeSet<>();
    public static String reasonCheckFileChanged = "Show";
    public static Thread closeActivity;
    public static ArrayList<String> LOG_REPORT = new ArrayList<>();

}
