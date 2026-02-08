package com.brosh.finance.monthlybudgetsync.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Utility class for input validation.
 * Provides common validation methods and EditText error handling.
 */
public final class ValidationUtil {
    
    /** Minimum password length */
    public static final int MIN_PASSWORD_LENGTH = 6;
    
    /** Maximum category name length */
    public static final int MAX_CATEGORY_NAME_LENGTH = 50;
    
    /** Maximum shop name length */
    public static final int MAX_SHOP_NAME_LENGTH = 50;
    
    private ValidationUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    // ============================================
    // STRING VALIDATION
    // ============================================
    
    /**
     * Checks if a string is null or empty (after trimming).
     * 
     * @param str the string to check
     * @return true if null, empty, or only whitespace
     */
    public static boolean isEmpty(@Nullable String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Checks if a string is not null and not empty.
     * 
     * @param str the string to check
     * @return true if not null and not empty
     */
    public static boolean isNotEmpty(@Nullable String str) {
        return !isEmpty(str);
    }
    
    /**
     * Safely trims a string.
     * 
     * @param str the string to trim
     * @return trimmed string, or empty string if null
     */
    @NonNull
    public static String safeTrim(@Nullable String str) {
        return str != null ? str.trim() : "";
    }
    
    // ============================================
    // EMAIL VALIDATION
    // ============================================
    
    /**
     * Validates an email address format.
     * 
     * @param email the email to validate
     * @return true if valid email format
     */
    public static boolean isValidEmail(@Nullable CharSequence email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Validates email and sets error on EditText if invalid.
     * 
     * @param emailField the EditText containing email
     * @param errorMessage the error message to show
     * @return true if valid, false if invalid
     */
    public static boolean validateEmail(@NonNull EditText emailField, @NonNull String errorMessage) {
        String email = emailField.getText().toString().trim();
        if (!isValidEmail(email)) {
            emailField.setError(errorMessage);
            emailField.requestFocus();
            return false;
        }
        return true;
    }
    
    // ============================================
    // PASSWORD VALIDATION
    // ============================================
    
    /**
     * Validates password meets minimum requirements.
     * 
     * @param password the password to validate
     * @return true if valid (not empty and meets length requirement)
     */
    public static boolean isValidPassword(@Nullable String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }
    
    /**
     * Validates password and sets error on EditText if invalid.
     * 
     * @param passwordField the EditText containing password
     * @param emptyError error message for empty password
     * @param lengthError error message for short password
     * @return true if valid, false if invalid
     */
    public static boolean validatePassword(@NonNull EditText passwordField, 
                                           @NonNull String emptyError,
                                           @NonNull String lengthError) {
        String password = passwordField.getText().toString();
        
        if (isEmpty(password)) {
            passwordField.setError(emptyError);
            passwordField.requestFocus();
            return false;
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordField.setError(lengthError);
            passwordField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates that two passwords match.
     * 
     * @param password the original password
     * @param confirmPassword the confirmation password
     * @return true if they match
     */
    public static boolean doPasswordsMatch(@Nullable String password, @Nullable String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
    
    // ============================================
    // NUMERIC VALIDATION
    // ============================================
    
    /**
     * Checks if a string is a valid positive number.
     * 
     * @param str the string to check
     * @return true if valid positive number
     */
    public static boolean isValidPositiveNumber(@Nullable String str) {
        if (isEmpty(str)) {
            return false;
        }
        
        try {
            double value = Double.parseDouble(str.replace(",", ""));
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Checks if a string is a valid non-negative number.
     * 
     * @param str the string to check
     * @return true if valid non-negative number
     */
    public static boolean isValidNonNegativeNumber(@Nullable String str) {
        if (isEmpty(str)) {
            return false;
        }
        
        try {
            double value = Double.parseDouble(str.replace(",", ""));
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Parses a string to double, returning default value on failure.
     * 
     * @param str the string to parse
     * @param defaultValue the default value if parsing fails
     * @return parsed value or default
     */
    public static double parseDouble(@Nullable String str, double defaultValue) {
        if (isEmpty(str)) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(str.replace(",", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Parses a string to int, returning default value on failure.
     * 
     * @param str the string to parse
     * @param defaultValue the default value if parsing fails
     * @return parsed value or default
     */
    public static int parseInt(@Nullable String str, int defaultValue) {
        if (isEmpty(str)) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(str.replace(",", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    // ============================================
    // EDITTEXT VALIDATION HELPERS
    // ============================================
    
    /**
     * Validates that an EditText is not empty.
     * 
     * @param editText the EditText to validate
     * @param errorMessage the error message to show
     * @return true if not empty, false if empty
     */
    public static boolean validateNotEmpty(@NonNull EditText editText, @NonNull String errorMessage) {
        String text = editText.getText().toString().trim();
        if (isEmpty(text)) {
            editText.setError(errorMessage);
            editText.requestFocus();
            return false;
        }
        return true;
    }
    
    /**
     * Validates that an EditText contains a positive number.
     * 
     * @param editText the EditText to validate
     * @param errorMessage the error message to show
     * @return true if valid positive number, false otherwise
     */
    public static boolean validatePositiveNumber(@NonNull EditText editText, @NonNull String errorMessage) {
        String text = editText.getText().toString().trim();
        if (!isValidPositiveNumber(text)) {
            editText.setError(errorMessage);
            editText.requestFocus();
            return false;
        }
        return true;
    }
    
    /**
     * Clears the error on an EditText.
     * 
     * @param editText the EditText to clear
     */
    public static void clearError(@NonNull EditText editText) {
        editText.setError(null);
    }
    
    // ============================================
    // SPECIAL CHARACTER VALIDATION
    // ============================================
    
    /**
     * Checks if a string contains illegal characters for Firebase keys.
     * Firebase keys cannot contain: . $ # [ ] /
     * 
     * @param str the string to check
     * @return true if contains illegal characters
     */
    public static boolean containsIllegalFirebaseChars(@Nullable String str) {
        if (str == null) {
            return false;
        }
        return str.contains(".") || str.contains("$") || str.contains("#") 
            || str.contains("[") || str.contains("]") || str.contains("/");
    }
    
    /**
     * Checks if a string contains the separator character.
     * 
     * @param str the string to check
     * @param separator the separator to check for
     * @return true if contains separator
     */
    public static boolean containsSeparator(@Nullable String str, @NonNull String separator) {
        return str != null && str.contains(separator);
    }
}
