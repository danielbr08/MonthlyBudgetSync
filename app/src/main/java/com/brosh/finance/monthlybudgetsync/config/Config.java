package com.brosh.finance.monthlybudgetsync.config;

import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public final class Config {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_FORMAT_CHARACTER = "/";
    public static final String SEPARATOR = "-";
    public static final char DOWN_ARROW = 'ꜜ';
    public static final char UP_ARROW = 'ꜛ';
    public static final String APP_URL = "https://play.google.com/store/apps/details?id=monthlybudget.apps.danielbrosh.monthlybudget&gl=IL"; // todo fill real app url from google play
    public static double CATEGORY_NAME_ET_WIDTH_PERCENT = 27.0 / 100;
    public static double CATEGORY_VALUE_ET_WIDTH_PERCENT = 14.0 / 100;
    public static double CONST_PAYMENT_CB_WIDTH_PERCENT = 14.0 / 100;
    public static double SHOP_ET_WIDTH_PERCENT = 23.0 / 100;
    public static double OPTIONAL_DAYS_SPINNER_WIDTH_PERCENT = 22.0 / 100;

    public static double CATEGORY_NAME_TV_TITLE_WIDTH_PERCENT = 27.0 / 100;
    public static double CATEGORY_VALUE_TV_TITLE_WIDTH_PERCENT = 17.0 / 100;
    public static double CONST_PAYMENT_TV_TITLE_WIDTH_PERCENT = 12.0 / 100;
    public static double SHOP_TV_TITLE_WIDTH_PERCENT = 22.0 / 100;
    public static double PAY_DATE_TITLE_WIDTH_PERCENT = 22.0 / 100;

    public static Set<String> shopsSet = new TreeSet<>();
    public static Thread closeActivity;
    public static ArrayList<String> LOG_REPORT = new ArrayList<>();

    public static final DatabaseReference DatabaseReferenceRoot = DBUtil.getDatabase().getReference();
    public static final DatabaseReference DatabaseReferenceUsers = DBUtil.getDatabase().getReference("Users");
    public static final DatabaseReference DatabaseReferenceShares = DBUtil.getDatabase().getReference("Shares");
    public static final DatabaseReference DatabaseReferenceMonthlyBudget = DBUtil.getDatabase().getReference("Monthly Budget");


}
