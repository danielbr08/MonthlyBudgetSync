package com.brosh.finance.monthlybudgetsync.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.config.Definitions;

/**
 * Utility class for text manipulation and display operations.
 * All methods are null-safe.
 */
public final class TextUtil {
    
    // Private constructor to prevent instantiation
    private TextUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Capitalizes the first letter of each word in a sentence.
     * 
     * @param sentence the sentence to capitalize
     * @param separator the character that separates words
     * @return capitalized sentence, or empty string if input is null/empty
     */
    @NonNull
    public static String getSentenceCapitalLetter(@Nullable String sentence, char separator) {
        if (sentence == null || sentence.isEmpty()) {
            return "";
        }
        
        int separatorIndex = sentence.indexOf(separator);
        if (separatorIndex == -1) {
            return getWordCapitalLetter(sentence);
        }
        
        return getWordCapitalLetter(sentence.substring(0, separatorIndex + 1)) 
             + getSentenceCapitalLetter(sentence.substring(separatorIndex + 1), separator);
    }

    /**
     * Capitalizes the first letter of a word.
     * 
     * @param word the word to capitalize
     * @return word with first letter capitalized, or empty string if input is null/empty
     */
    @NonNull
    public static String getWordCapitalLetter(@Nullable String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }
        
        char firstLetter = word.charAt(0);
        // Check if lowercase letter (a-z in ASCII)
        if (firstLetter >= 'a' && firstLetter <= 'z') {
            firstLetter = Character.toUpperCase(firstLetter);
        }
        
        return firstLetter + word.substring(1);
    }

    /**
     * Returns the standard separator string.
     * @return arrow separator string
     */
    @NonNull
    public static String getSeparator() {
        return Definitions.ARROW_RIGHT;
    }

    /**
     * Shows a toast message on the UI thread.
     * Safe to call from any thread.
     * 
     * @param message the message to display
     * @param duration Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     * @param context the context for creating the toast
     */
    public static void showMessage(@Nullable String message, int duration, @Nullable Context context) {
        if (context == null || message == null) {
            return;
        }
        
        // Ensure toast is shown on main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context, message, duration).show();
        } else {
            new Handler(Looper.getMainLooper()).post(() -> 
                Toast.makeText(context, message, duration).show()
            );
        }
    }

    /**
     * Converts email address dots to commas (for Firebase key compatibility).
     * 
     * @param email the email address
     * @return email with dots replaced by commas, or empty string if null
     */
    @NonNull
    public static String getEmailComma(@Nullable String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }
        return email.trim().replace(Definitions.DOT, Definitions.COMMA);
    }

    /**
     * Validates an email address format.
     * 
     * @param email the email to validate
     * @return true if valid email format, false otherwise
     */
    public static boolean isEmailValid(@Nullable CharSequence email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Checks if a string is null or empty after trimming.
     * 
     * @param str the string to check
     * @return true if null, empty, or only whitespace
     */
    @SuppressWarnings("unused") // May be used for future functionality
    public static boolean isEmpty(@Nullable String str) {
        return str == null || str.trim().isEmpty();
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
}
