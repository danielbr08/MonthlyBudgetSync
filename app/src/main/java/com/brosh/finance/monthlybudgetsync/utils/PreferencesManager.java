package com.brosh.finance.monthlybudgetsync.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Centralized manager for SharedPreferences operations.
 * Supports both singleton pattern (via initialize/getInstance) and instance creation.
 * Reduces code duplication and provides type-safe accessors.
 */
public final class PreferencesManager {
    
    private static final String PREFS_NAME = "checkbox";
    
    // Preference keys
    public static final String KEY_REMEMBER_ME = "rememberMe";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    
    // Default values
    private static final String DEFAULT_STRING = "";
    private static final boolean DEFAULT_BOOLEAN = false;
    
    // Singleton instance
    @Nullable
    private static PreferencesManager instance;
    
    private final SharedPreferences preferences;
    
    /**
     * Initializes the singleton instance with application context.
     * Call this once in Application.onCreate().
     * 
     * @param context the application context
     */
    public static synchronized void initialize(@NonNull Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context.getApplicationContext());
        }
    }
    
    /**
     * Gets the singleton instance.
     * Must call initialize() first.
     * 
     * @return the PreferencesManager instance
     * @throws IllegalStateException if not initialized
     */
    @NonNull
    public static PreferencesManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PreferencesManager not initialized. Call initialize() first.");
        }
        return instance;
    }
    
    /**
     * Checks if the singleton is initialized.
     * 
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return instance != null;
    }
    
    /**
     * Creates a new PreferencesManager instance.
     * For most cases, use getInstance() instead after calling initialize().
     * 
     * @param context the application or activity context
     */
    public PreferencesManager(@NonNull Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // ============================================
    // STRING OPERATIONS
    // ============================================
    
    /**
     * Gets a string preference value.
     * 
     * @param key the preference key
     * @return the value, or empty string if not found
     */
    @NonNull
    public String getString(@NonNull String key) {
        return preferences.getString(key, DEFAULT_STRING);
    }
    
    /**
     * Gets a string preference value with a custom default.
     * 
     * @param key the preference key
     * @param defaultValue the default value if not found
     * @return the value, or defaultValue if not found
     */
    @NonNull
    public String getString(@NonNull String key, @NonNull String defaultValue) {
        return preferences.getString(key, defaultValue);
    }
    
    /**
     * Sets a string preference value.
     * 
     * @param key the preference key
     * @param value the value to set
     */
    public void setString(@NonNull String key, @Nullable String value) {
        preferences.edit().putString(key, value).apply();
    }
    
    // ============================================
    // BOOLEAN OPERATIONS
    // ============================================
    
    /**
     * Gets a boolean preference value.
     * 
     * @param key the preference key
     * @return the value, or false if not found
     */
    public boolean getBoolean(@NonNull String key) {
        // Handle legacy "true"/"false" string storage
        String value = preferences.getString(key, null);
        if (value != null) {
            return "true".equalsIgnoreCase(value);
        }
        return preferences.getBoolean(key, DEFAULT_BOOLEAN);
    }
    
    /**
     * Sets a boolean preference value.
     * Stores as string for legacy compatibility.
     * 
     * @param key the preference key
     * @param value the value to set
     */
    public void setBoolean(@NonNull String key, boolean value) {
        preferences.edit().putString(key, value ? "true" : "false").apply();
    }
    
    // ============================================
    // INTEGER OPERATIONS
    // ============================================
    
    /**
     * Gets an integer preference value.
     * 
     * @param key the preference key
     * @param defaultValue the default value if not found
     * @return the value, or defaultValue if not found
     */
    public int getInt(@NonNull String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }
    
    /**
     * Sets an integer preference value.
     * 
     * @param key the preference key
     * @param value the value to set
     */
    public void setInt(@NonNull String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }
    
    // ============================================
    // UTILITY OPERATIONS
    // ============================================
    
    /**
     * Removes a preference value.
     * 
     * @param key the preference key to remove
     */
    public void remove(@NonNull String key) {
        preferences.edit().remove(key).apply();
    }
    
    /**
     * Clears all preferences.
     */
    public void clear() {
        preferences.edit().clear().apply();
    }
    
    /**
     * Checks if a preference exists.
     * 
     * @param key the preference key
     * @return true if the key exists
     */
    public boolean contains(@NonNull String key) {
        return preferences.contains(key);
    }
    
    // ============================================
    // CONVENIENCE METHODS FOR LOGIN
    // ============================================
    
    /**
     * Checks if "Remember Me" is enabled.
     * 
     * @return true if remember me is enabled
     */
    public boolean isRememberMeEnabled() {
        return getBoolean(KEY_REMEMBER_ME);
    }
    
    /**
     * Sets the "Remember Me" preference.
     * 
     * @param enabled true to enable remember me
     */
    public void setRememberMe(boolean enabled) {
        setBoolean(KEY_REMEMBER_ME, enabled);
    }
    
    /**
     * Gets the saved email address.
     * 
     * @return the email, or empty string if not saved
     */
    @NonNull
    public String getSavedEmail() {
        return getString(KEY_EMAIL);
    }
    
    /**
     * Gets the saved password.
     * 
     * @return the password, or empty string if not saved
     */
    @NonNull
    public String getSavedPassword() {
        return getString(KEY_PASSWORD);
    }
    
    /**
     * Saves login credentials.
     * 
     * @param email the email to save
     * @param password the password to save
     */
    public void saveCredentials(@NonNull String email, @NonNull String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }
    
    /**
     * Clears saved login credentials.
     */
    public void clearCredentials() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.apply();
    }
}
