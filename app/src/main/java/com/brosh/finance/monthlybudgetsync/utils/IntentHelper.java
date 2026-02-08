package com.brosh.finance.monthlybudgetsync.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Month;

import java.io.Serializable;

/**
 * Helper class for Intent operations.
 * Provides safe methods for putting and retrieving intent extras.
 */
public final class IntentHelper {
    
    private IntentHelper() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    // ============================================
    // INTENT CREATION
    // ============================================
    
    /**
     * Creates an intent with the month parameter.
     * 
     * @param context the context
     * @param targetClass the target activity class
     * @param month the month to pass (can be null)
     * @return configured intent
     */
    @NonNull
    public static Intent createIntentWithMonth(@NonNull Context context, 
                                               @NonNull Class<?> targetClass,
                                               @Nullable Month month) {
        Intent intent = new Intent(context, targetClass);
        putMonth(intent, month);
        return intent;
    }
    
    /**
     * Creates an intent with year-month string parameter.
     * 
     * @param context the context
     * @param targetClass the target activity class
     * @param yearMonth the year-month string (can be null)
     * @return configured intent
     */
    @NonNull
    public static Intent createIntentWithYearMonth(@NonNull Context context,
                                                   @NonNull Class<?> targetClass,
                                                   @Nullable String yearMonth) {
        Intent intent = new Intent(context, targetClass);
        putYearMonth(intent, yearMonth);
        return intent;
    }
    
    // ============================================
    // PUT METHODS
    // ============================================
    
    /**
     * Puts the month's yearMonth string into the intent.
     * 
     * @param intent the intent
     * @param month the month (can be null)
     */
    public static void putMonth(@NonNull Intent intent, @Nullable Month month) {
        intent.putExtra(Definitions.MONTH, month != null ? month.getYearMonth() : null);
    }
    
    /**
     * Puts a year-month string into the intent.
     * 
     * @param intent the intent
     * @param yearMonth the year-month string (can be null)
     */
    public static void putYearMonth(@NonNull Intent intent, @Nullable String yearMonth) {
        intent.putExtra(Definitions.MONTH, yearMonth);
    }
    
    /**
     * Puts a string extra into the intent safely.
     * 
     * @param intent the intent
     * @param key the extra key
     * @param value the value (can be null)
     */
    public static void putString(@NonNull Intent intent, @NonNull String key, @Nullable String value) {
        intent.putExtra(key, value);
    }
    
    /**
     * Puts a serializable extra into the intent safely.
     * 
     * @param intent the intent
     * @param key the extra key
     * @param value the serializable value (can be null)
     */
    public static void putSerializable(@NonNull Intent intent, @NonNull String key, @Nullable Serializable value) {
        if (value != null) {
            intent.putExtra(key, value);
        }
    }
    
    /**
     * Puts an integer extra into the intent.
     * 
     * @param intent the intent
     * @param key the extra key
     * @param value the integer value
     */
    public static void putInt(@NonNull Intent intent, @NonNull String key, int value) {
        intent.putExtra(key, value);
    }
    
    /**
     * Puts a boolean extra into the intent.
     * 
     * @param intent the intent
     * @param key the extra key
     * @param value the boolean value
     */
    public static void putBoolean(@NonNull Intent intent, @NonNull String key, boolean value) {
        intent.putExtra(key, value);
    }
    
    // ============================================
    // GET METHODS
    // ============================================
    
    /**
     * Gets the month parameter from an activity's intent.
     * 
     * @param activity the activity
     * @return year-month string or null
     */
    @Nullable
    public static String getYearMonth(@NonNull Activity activity) {
        return getYearMonth(activity.getIntent());
    }
    
    /**
     * Gets the month parameter from an intent.
     * 
     * @param intent the intent
     * @return year-month string or null
     */
    @Nullable
    public static String getYearMonth(@Nullable Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            return null;
        }
        return intent.getStringExtra(Definitions.MONTH);
    }
    
    /**
     * Gets a string extra from an activity's intent.
     * 
     * @param activity the activity
     * @param key the extra key
     * @param defaultValue the default value if not found
     * @return the string value or default
     */
    @NonNull
    public static String getString(@NonNull Activity activity, @NonNull String key, @NonNull String defaultValue) {
        return getString(activity.getIntent(), key, defaultValue);
    }
    
    /**
     * Gets a string extra from an intent.
     * 
     * @param intent the intent
     * @param key the extra key
     * @param defaultValue the default value if not found
     * @return the string value or default
     */
    @NonNull
    public static String getString(@Nullable Intent intent, @NonNull String key, @NonNull String defaultValue) {
        if (intent == null) {
            return defaultValue;
        }
        String value = intent.getStringExtra(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Gets an integer extra from an activity's intent.
     * 
     * @param activity the activity
     * @param key the extra key
     * @param defaultValue the default value if not found
     * @return the integer value or default
     */
    public static int getInt(@NonNull Activity activity, @NonNull String key, int defaultValue) {
        return getInt(activity.getIntent(), key, defaultValue);
    }
    
    /**
     * Gets an integer extra from an intent.
     * 
     * @param intent the intent
     * @param key the extra key
     * @param defaultValue the default value if not found
     * @return the integer value or default
     */
    public static int getInt(@Nullable Intent intent, @NonNull String key, int defaultValue) {
        if (intent == null) {
            return defaultValue;
        }
        return intent.getIntExtra(key, defaultValue);
    }
    
    /**
     * Gets a boolean extra from an activity's intent.
     * 
     * @param activity the activity
     * @param key the extra key
     * @param defaultValue the default value if not found
     * @return the boolean value or default
     */
    public static boolean getBoolean(@NonNull Activity activity, @NonNull String key, boolean defaultValue) {
        return getBoolean(activity.getIntent(), key, defaultValue);
    }
    
    /**
     * Gets a boolean extra from an intent.
     * 
     * @param intent the intent
     * @param key the extra key
     * @param defaultValue the default value if not found
     * @return the boolean value or default
     */
    public static boolean getBoolean(@Nullable Intent intent, @NonNull String key, boolean defaultValue) {
        if (intent == null) {
            return defaultValue;
        }
        return intent.getBooleanExtra(key, defaultValue);
    }
    
    /**
     * Checks if an intent has a specific extra.
     * 
     * @param intent the intent
     * @param key the extra key
     * @return true if the extra exists
     */
    public static boolean hasExtra(@Nullable Intent intent, @NonNull String key) {
        return intent != null && intent.hasExtra(key);
    }
    
    // ============================================
    // NAVIGATION HELPERS
    // ============================================
    
    /**
     * Starts an activity with the month parameter.
     * 
     * @param activity the current activity
     * @param targetClass the target activity class
     * @param month the month to pass
     */
    public static void startActivityWithMonth(@NonNull Activity activity,
                                              @NonNull Class<?> targetClass,
                                              @Nullable Month month) {
        Intent intent = createIntentWithMonth(activity, targetClass, month);
        activity.startActivity(intent);
    }
    
    /**
     * Starts an activity with year-month parameter.
     * 
     * @param activity the current activity
     * @param targetClass the target activity class
     * @param yearMonth the year-month string
     */
    public static void startActivityWithYearMonth(@NonNull Activity activity,
                                                  @NonNull Class<?> targetClass,
                                                  @Nullable String yearMonth) {
        Intent intent = createIntentWithYearMonth(activity, targetClass, yearMonth);
        activity.startActivity(intent);
    }
    
    /**
     * Starts an activity and finishes the current one.
     * 
     * @param activity the current activity
     * @param targetClass the target activity class
     */
    public static void startActivityAndFinish(@NonNull Activity activity,
                                              @NonNull Class<?> targetClass) {
        activity.startActivity(new Intent(activity, targetClass));
        activity.finish();
    }
}
