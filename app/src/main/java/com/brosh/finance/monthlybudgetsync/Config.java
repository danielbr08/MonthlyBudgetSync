package com.brosh.finance.monthlybudgetsync;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public final class Config {
    public static final String dateFormat = "dd/MM/yyyy";
    public static final String dateFormat2 =  "EEE MMM dd HH:mm:ss zzz yyyy";
    public static String SEPARATOR = "-->";
    public static char DOWN_ARROW = 'ꜜ';
    public static char UP_ARROW = 'ꜛ';
    public static Set<String> shopsSet = new TreeSet<>();

    public static String reasonCheckFileChanged = "Show";

    public static Thread closeActivity;

    public static String DEFAULT_LANGUAGE;
    public static boolean IS_AD_ENEABLED = true;
    public static ArrayList<String> LOG_REPORT = new ArrayList<>();
}
