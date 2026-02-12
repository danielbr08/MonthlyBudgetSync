package com.brosh.finance.monthlybudgetsync.utils;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseError;

/**
 * Centralized handler for Firebase errors.
 * Provides consistent error messages and handling across the app.
 */
public final class FirebaseErrorHandler {
    
    private static final String TAG = "FirebaseErrorHandler";
    
    private FirebaseErrorHandler() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    // ============================================
    // AUTH ERROR CODES
    // ============================================
    
    public static final String ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL";
    public static final String ERROR_WRONG_PASSWORD = "ERROR_WRONG_PASSWORD";
    public static final String ERROR_INVALID_CREDENTIAL = "ERROR_INVALID_CREDENTIAL";
    public static final String ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND";
    public static final String ERROR_USER_DISABLED = "ERROR_USER_DISABLED";
    public static final String ERROR_TOO_MANY_REQUESTS = "ERROR_TOO_MANY_REQUESTS";
    public static final String ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE";
    public static final String ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD";
    public static final String ERROR_OPERATION_NOT_ALLOWED = "ERROR_OPERATION_NOT_ALLOWED";
    
    // ============================================
    // LOGIN ERROR HANDLING
    // ============================================
    
    /**
     * Handles login errors and sets appropriate error messages on fields.
     * 
     * @param context the context for string resources
     * @param exception the exception that occurred
     * @param emailField the email EditText (for setting errors)
     * @param passwordField the password EditText (for setting errors)
     * @return true if error was handled, false otherwise
     */
    public static boolean handleLoginError(@NonNull Context context,
                                           @Nullable Exception exception,
                                           @Nullable EditText emailField,
                                           @Nullable EditText passwordField) {
        if (exception == null) {
            showGenericError(context);
            return true;
        }
        
        Log.e(TAG, "Login error: " + exception.getMessage(), exception);
        
        if (exception instanceof FirebaseNetworkException) {
            TextUtil.showMessage(context.getString(R.string.network_error), Toast.LENGTH_SHORT, context);
            return true;
        }
        
        if (exception instanceof FirebaseAuthException authException) {
            return handleAuthError(context, authException, emailField, passwordField);
        }
        
        showGenericError(context);
        return true;
    }
    
    /**
     * Handles Firebase Auth exceptions and sets appropriate field errors.
     */
    private static boolean handleAuthError(@NonNull Context context,
                                           @NonNull FirebaseAuthException exception,
                                           @Nullable EditText emailField,
                                           @Nullable EditText passwordField) {
        String errorCode = exception.getErrorCode();
        
        switch (errorCode) {
            case ERROR_INVALID_EMAIL:
                if (emailField != null) {
                    emailField.setError(context.getString(R.string.error_invalid_email));
                    emailField.requestFocus();
                }
                return true;
                
            case ERROR_WRONG_PASSWORD:
            case ERROR_INVALID_CREDENTIAL:
                if (passwordField != null) {
                    passwordField.setError(context.getString(R.string.error_invalid_password));
                    passwordField.requestFocus();
                }
                return true;
                
            case ERROR_USER_NOT_FOUND:
                if (emailField != null) {
                    emailField.setError(context.getString(R.string.user_not_found));
                    emailField.requestFocus();
                }
                return true;
                
            case ERROR_USER_DISABLED:
                TextUtil.showMessage(context.getString(R.string.account_disabled), Toast.LENGTH_LONG, context);
                return true;
                
            case ERROR_TOO_MANY_REQUESTS:
                TextUtil.showMessage(context.getString(R.string.too_many_attempts), Toast.LENGTH_LONG, context);
                return true;
                
            default:
                showGenericError(context);
                return true;
        }
    }
    
    // ============================================
    // REGISTRATION ERROR HANDLING
    // ============================================
    
    /**
     * Handles registration errors and sets appropriate error messages on fields.
     * 
     * @param context the context for string resources
     * @param exception the exception that occurred
     * @param emailField the email EditText (for setting errors)
     * @param passwordField the password EditText (for setting errors)
     * @return true if error was handled, false otherwise
     */
    public static boolean handleRegistrationError(@NonNull Context context,
                                                  @Nullable Exception exception,
                                                  @Nullable EditText emailField,
                                                  @Nullable EditText passwordField) {
        if (exception == null) {
            showGenericError(context);
            return true;
        }
        
        Log.e(TAG, "Registration error: " + exception.getMessage(), exception);
        
        if (exception instanceof FirebaseNetworkException) {
            TextUtil.showMessage(context.getString(R.string.network_error), Toast.LENGTH_SHORT, context);
            return true;
        }
        
        if (exception instanceof FirebaseAuthException authException) {
            return handleRegistrationAuthError(context, authException, emailField, passwordField);
        }
        
        showGenericError(context);
        return true;
    }
    
    /**
     * Handles Firebase Auth exceptions during registration.
     */
    private static boolean handleRegistrationAuthError(@NonNull Context context,
                                                       @NonNull FirebaseAuthException exception,
                                                       @Nullable EditText emailField,
                                                       @Nullable EditText passwordField) {
        String errorCode = exception.getErrorCode();
        
        switch (errorCode) {
            case ERROR_INVALID_EMAIL:
                if (emailField != null) {
                    emailField.setError(context.getString(R.string.error_invalid_email));
                    emailField.requestFocus();
                }
                return true;
                
            case ERROR_EMAIL_ALREADY_IN_USE:
                if (emailField != null) {
                    emailField.setError(context.getString(R.string.error_email_already_in_use));
                    emailField.requestFocus();
                }
                return true;
                
            case ERROR_WEAK_PASSWORD:
                if (passwordField != null) {
                    passwordField.setError(context.getString(R.string.error_weak_password));
                    passwordField.requestFocus();
                }
                return true;
                
            case ERROR_OPERATION_NOT_ALLOWED:
                TextUtil.showMessage(context.getString(R.string.email_password_not_enabled), Toast.LENGTH_LONG, context);
                return true;
                
            default:
                showGenericError(context);
                return true;
        }
    }
    
    // ============================================
    // DATABASE ERROR HANDLING
    // ============================================
    
    /**
     * Handles Firebase Database errors.
     * 
     * @param context the context for string resources
     * @param error the database error
     */
    public static void handleDatabaseError(@NonNull Context context, @Nullable DatabaseError error) {
        if (error == null) {
            return;
        }
        
        Log.e(TAG, "Database error: " + error.getMessage());
        
        switch (error.getCode()) {
            case DatabaseError.DISCONNECTED:
            case DatabaseError.NETWORK_ERROR:
                TextUtil.showMessage(context.getString(R.string.network_error), Toast.LENGTH_SHORT, context);
                break;
                
            case DatabaseError.PERMISSION_DENIED:
                TextUtil.showMessage(context.getString(R.string.permission_denied), Toast.LENGTH_LONG, context);
                break;
                
            case DatabaseError.DATA_STALE:
            case DatabaseError.EXPIRED_TOKEN:
                TextUtil.showMessage(context.getString(R.string.session_expired), Toast.LENGTH_LONG, context);
                break;
                
            default:
                showGenericError(context);
                break;
        }
    }
    
    // ============================================
    // UTILITY METHODS
    // ============================================
    
    /**
     * Shows a generic error message.
     * 
     * @param context the context
     */
    public static void showGenericError(@NonNull Context context) {
        TextUtil.showMessage(context.getString(R.string.error), Toast.LENGTH_SHORT, context);
    }
    
    /**
     * Logs an exception with the given tag.
     * 
     * @param tag the log tag
     * @param message the log message
     * @param exception the exception to log
     */
    public static void logError(@NonNull String tag, @NonNull String message, @Nullable Exception exception) {
        if (exception != null) {
            Log.e(tag, message + ": " + exception.getMessage(), exception);
        } else {
            Log.e(tag, message);
        }
    }
}
