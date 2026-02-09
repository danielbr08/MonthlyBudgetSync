package com.brosh.finance.monthlybudgetsync.config;

/**
 * Application-wide string constants and definitions.
 * Organized by category for better maintainability.
 */
public final class Definitions {
    
    private Definitions() {
        throw new AssertionError("Constants class - do not instantiate");
    }
    
    // ============================================
    // FIREBASE NODE NAMES
    // ============================================
    
    /** Categories node in Firebase */
    public static final String CATEGORIES = "categories";
    
    /** Transactions node in Firebase */
    public static final String TRANSACTIONS = "transactions";
    
    /** Monthly Budget root node in Firebase */
    public static final String MONTHLY_BUDGET = "Monthly Budget";
    
    /** Budget node in Firebase */
    public static final String BUDGETS = "Budget";
    
    /** Months collection node in Firebase */
    public static final String MONTHS = "Months";
    
    /** Single month intent key */
    public static final String MONTH = "Month";
    
    /** Shops node in Firebase */
    public static final String SHOPS = "Shops";
    
    /** Owners node in Firebase */
    public static final String OWNERS = "Owners";
    
    /** Shares node in Firebase */
    public static final String SHARES = "Shares";
    
    /** Users node in Firebase */
    public static final String USERS = "Users";
    
    /** Email-UID mapping node in Firebase */
    public static final String EMAIL_UID = "Email Uid";
    
    /** Contact us node in Firebase */
    public static final String CONTACT_US = "contactUs";
    
    // ============================================
    // FIREBASE FIELD NAMES
    // ============================================
    
    /** Balance field name */
    public static final String BALANCE = "balance";
    
    /** Budget field name */
    public static final String BUDGET = "budget";
    
    /** Name field name */
    public static final String NAME = "name";
    
    /** Deleted flag field name */
    public static final String DELETED = "deleted";
    
    /** Budget number field name */
    public static final String BUDGET_NUMBER = "budgetNumber";
    
    /** Transaction ID numerator field name */
    public static final String TRAN_ID_NUMERATOR = "tranIdNumerator";
    
    /** User settings node name */
    public static final String USER_SETTINGS = "userSettings";
    
    /** Database key field name */
    public static final String DBKEY = "dbKey";
    
    /** Charge day field name */
    public static final String CHARGE_DAY = "chargeDay";
    
    /** Currency field name */
    public static final String CURRENCY = "currency";
    
    /** Profile node name */
    public static final String PROFILE = "profile";
    
    // ============================================
    // INTENT EXTRAS
    // ============================================
    
    /** User extra key for intents */
    public static final String USER = "User";
    
    /** Update type extra key */
    public static final String UPDATE_TYPE = "updateType";
    
    // ============================================
    // STRING CONSTANTS
    // ============================================
    
    /** Zero string constant */
    public static final String ZERO = "0";
    
    /** Dot character */
    public static final String DOT = ".";
    
    /** Comma character */
    public static final String COMMA = ",";
    
    /** Right arrow separator */
    public static final String ARROW_RIGHT = "->";
    
    /** Dash character */
    public static final String DASH = "-";
    
    // ============================================
    // UI SYMBOLS
    // ============================================
    
    /** Up arrow character for ascending sort */
    public static final char ARROW_UP = 'ꜛ';
    
    /** Down arrow character for descending sort */
    public static final char ARROW_DOWN = 'ꜜ';
    
    // ============================================
    // LANGUAGE CODES
    // ============================================
    
    /** Hebrew language code */
    public static final String HEBREW = "HEB";
    
    /** English language code */
    public static final String ENGLISH = "EN";
    
    // ============================================
    // OPERATION CODES
    // ============================================
    
    /** Create operation code */
    public static final String CREATE_CODE = "CRT";
    
    /** Add operation code */
    public static final String ADD_CODE = "ADD";
    
    /** Delete operation code */
    public static final String DELETE_CODE = "DEL";
    
    // ============================================
    // SORT CONSTANTS
    // ============================================
    
    /** Sort by ID */
    public static final int SORT_BY_ID = 1;
    
    /** Sort by category name */
    public static final int SORT_BY_CATEGORY = 2;
    
    /** Sort by payment method */
    public static final int SORT_BY_PAYMENT_METHOD = 3;
    
    /** Sort by store name */
    public static final int SORT_BY_STORE = 4;
    
    /** Sort by charge date */
    public static final int SORT_BY_CHARGE_DATE = 5;
    
    /** Sort by price */
    public static final int SORT_BY_PRICE = 6;
    
    /** Sort by registration date */
    public static final int SORT_BY_REGISTRATION_DATE = 7;
    
    // ============================================
    // UPDATE TYPE CONSTANTS
    // ============================================
    
    /** Update email type */
    public static final int UPDATE_EMAIL = 1;
    
    /** Update password type */
    public static final int UPDATE_PASSWORD = 2;
    
    /** Update phone number type */
    public static final int UPDATE_PHONE_NUMBER = 3;
    
    /** Update user name type */
    public static final int UPDATE_USER_NAME = 4;
    
    // ============================================
    // SETTINGS KEYS
    // ============================================
    
    /** Default show active only setting key */
    public static final String DEFAULT_SHOW_ACTIVE_ONLY = "defaultShowActiveOnly";
    
    /** Auto complete setting key */
    public static final String AUTO_COMPLETE = "autoComplete";
    
    /** Change user name setting key */
    public static final String CHANGE_USER_NAME = "changeUserName";
    
    /** Change email setting key */
    public static final String CHANGE_EMAIL = "changeEmail";
    
    /** Change password setting key */
    public static final String CHANGE_PASSWORD = "changePassword";
    
    /** Change phone setting key */
    public static final String CHANGE_PHONE = "changePhone";
    
    /** Email updates setting key */
    public static final String EMAIL_UPDATES = "emailUpdates";
    
    /** Notifications setting key */
    public static final String NOTIFICATIONS = "notifications";
    
    /** Allow edit previous months setting key */
    public static final String ALLOW_EDIT_PREVIOUS_MONTHS = "allowEditPreviousMonths";
    
    // ============================================
    // ERROR MESSAGES
    // ============================================
    
    /** Email already shared error message */
    public static final String EMAIL_ALREADY_SHARED = "Email already shared";
}
