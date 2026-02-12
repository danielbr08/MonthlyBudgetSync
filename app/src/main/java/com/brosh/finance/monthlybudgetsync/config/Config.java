package com.brosh.finance.monthlybudgetsync.config;

import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.google.firebase.database.DatabaseReference;

/**
 * Application configuration constants.
 * Organized by category for better maintainability.
 */
public final class Config {
    
    private Config() {
        throw new AssertionError("Constants class - do not instantiate");
    }
    
    // ============================================
    // PREMIUM FEATURE FLAGS (for future use)
    // ============================================
    
    /** 
     * If true, premium features are enabled for this build.
     * In future: This will be determined by user's subscription status.
     * When false, "Show all languages" and "Show all currencies" options
     * in Settings will be disabled/hidden for non-premium users.
     */
    public static final boolean PREMIUM_FEATURES_ENABLED = true;
    
    // ============================================
    // DATE FORMATTING
    // ============================================
    
    /** Date format pattern (day/month/year) */
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    
    /** Short date format pattern for display (day/month/2-digit year) */
    public static final String DATE_FORMAT_SHORT = "dd/MM/yy";
    
    /** Character used as date separator */
    public static final String DATE_FORMAT_CHARACTER = "/";
    
    /** Separator for year-month strings (e.g., "2024-01") */
    public static final String SEPARATOR = "-";
    
    // ============================================
    // UI CONSTANTS
    // ============================================
    
    /** Down arrow character for descending sort */
    public static final char DOWN_ARROW = 'ꜜ';
    
    /** Up arrow character for ascending sort */
    public static final char UP_ARROW = 'ꜛ';
    
    // ============================================
    // APPLICATION URLS
    // ============================================
    
    /** Play Store URL for the app */
    public static final String APP_URL = "https://play.google.com/store/apps/details?id=com.brosh.finance.monthlybudgetsync";
    
    // ============================================
    // LAYOUT WIDTH PERCENTAGES - CREATE BUDGET DATA WIDGETS
    // ============================================
    
    /** Width percentage for category name EditText */
    public static final double CATEGORY_NAME_ET_WIDTH_PERCENT = 0.27;
    
    /** Width percentage for category value EditText */
    public static final double CATEGORY_VALUE_ET_WIDTH_PERCENT = 0.14;
    
    /** Width percentage for constant payment CheckBox */
    public static final double CONST_PAYMENT_CB_WIDTH_PERCENT = 0.14;
    
    /** Width percentage for shop EditText */
    public static final double SHOP_ET_WIDTH_PERCENT = 0.23;
    
    /** Width percentage for optional days Spinner */
    public static final double OPTIONAL_DAYS_SPINNER_WIDTH_PERCENT = 0.22;
    
    // ============================================
    // LAYOUT WIDTH PERCENTAGES - CREATE BUDGET TITLE WIDGETS
    // ============================================
    
    /** Width percentage for category name title */
    public static final double CATEGORY_NAME_TV_TITLE_WIDTH_PERCENT = 0.27;
    
    /** Width percentage for category value title */
    public static final double CATEGORY_VALUE_TV_TITLE_WIDTH_PERCENT = 0.17;
    
    /** Width percentage for constant payment title */
    public static final double CONST_PAYMENT_TV_TITLE_WIDTH_PERCENT = 0.12;
    
    /** Width percentage for shop title */
    public static final double SHOP_TV_TITLE_WIDTH_PERCENT = 0.22;
    
    /** Width percentage for pay date title */
    public static final double PAY_DATE_TITLE_WIDTH_PERCENT = 0.22;
    
    // ============================================
    // FIREBASE DATABASE REFERENCES
    // ============================================
    
    /** Root database reference */
    public static final DatabaseReference DatabaseReferenceRoot = DBUtil.getDatabase().getReference();
    
    /** Users collection reference */
    public static final DatabaseReference DatabaseReferenceUsers = DBUtil.getDatabase().getReference(Definitions.USERS);
    
    /** Shares collection reference */
    public static final DatabaseReference DatabaseReferenceShares = DBUtil.getDatabase().getReference(Definitions.SHARES);
    
    /** Owners collection reference */
    public static final DatabaseReference DatabaseReferenceOwners = DBUtil.getDatabase().getReference(Definitions.OWNERS);
    
    /** Monthly Budget collection reference */
    public static final DatabaseReference DatabaseReferenceMonthlyBudget = DBUtil.getDatabase().getReference(Definitions.MONTHLY_BUDGET);
}
