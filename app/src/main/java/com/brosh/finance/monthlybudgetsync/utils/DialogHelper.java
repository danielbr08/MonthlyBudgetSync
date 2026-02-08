package com.brosh.finance.monthlybudgetsync.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.brosh.finance.monthlybudgetsync.R;

/**
 * Helper class for creating common dialogs.
 * Reduces code duplication and provides consistent dialog styling.
 */
public final class DialogHelper {
    
    private DialogHelper() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    // ============================================
    // CONFIRMATION DIALOGS
    // ============================================
    
    /**
     * Shows a simple confirmation dialog with Yes/No buttons.
     * 
     * @param context the context
     * @param message the message to display
     * @param onYes callback when Yes is clicked
     * @param onNo callback when No is clicked (can be null)
     */
    public static void showConfirmation(@NonNull Context context,
                                        @NonNull String message,
                                        @NonNull Runnable onYes,
                                        @Nullable Runnable onNo) {
        new AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(context.getString(R.string.yes), (dialog, id) -> onYes.run())
            .setNegativeButton(context.getString(R.string.no), (dialog, id) -> {
                if (onNo != null) {
                    onNo.run();
                }
            })
            .show();
    }
    
    /**
     * Shows a confirmation dialog with custom button texts.
     * 
     * @param context the context
     * @param message the message to display
     * @param positiveText positive button text
     * @param negativeText negative button text
     * @param onPositive callback when positive button is clicked
     * @param onNegative callback when negative button is clicked (can be null)
     */
    public static void showConfirmation(@NonNull Context context,
                                        @NonNull String message,
                                        @NonNull String positiveText,
                                        @NonNull String negativeText,
                                        @NonNull Runnable onPositive,
                                        @Nullable Runnable onNegative) {
        new AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(positiveText, (dialog, id) -> onPositive.run())
            .setNegativeButton(negativeText, (dialog, id) -> {
                if (onNegative != null) {
                    onNegative.run();
                }
            })
            .show();
    }
    
    /**
     * Shows an exit confirmation dialog.
     * 
     * @param context the context
     * @param onConfirmExit callback when exit is confirmed
     */
    public static void showExitConfirmation(@NonNull Context context, @NonNull Runnable onConfirmExit) {
        showConfirmation(
            context,
            context.getString(R.string.are_you_sure_you_want_to_exit),
            onConfirmExit,
            null
        );
    }
    
    // ============================================
    // INPUT DIALOGS
    // ============================================
    
    /**
     * Shows a dialog with an email input field.
     * 
     * @param context the context
     * @param title the dialog title
     * @param hint the input hint
     * @param positiveText positive button text
     * @param onSubmit callback with the entered email when submitted
     */
    public static void showEmailInputDialog(@NonNull Context context,
                                            @NonNull String title,
                                            @NonNull String hint,
                                            @NonNull String positiveText,
                                            @NonNull EmailInputCallback onSubmit) {
        final EditText emailInput = new EditText(context);
        emailInput.setHint(hint);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setView(emailInput)
            .setPositiveButton(positiveText, null) // Set to null initially
            .setNegativeButton(context.getString(R.string.cancel), (d, which) -> d.cancel())
            .create();
        
        dialog.show();
        
        // Override positive button to validate before dismissing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            
            if (!ValidationUtil.isValidEmail(email)) {
                emailInput.setError(context.getString(R.string.invalid_email));
                return;
            }
            
            onSubmit.onEmailSubmitted(email, dialog);
        });
    }
    
    /**
     * Shows a dialog with a text input field.
     * 
     * @param context the context
     * @param title the dialog title
     * @param hint the input hint
     * @param positiveText positive button text
     * @param onSubmit callback with the entered text when submitted
     */
    public static void showTextInputDialog(@NonNull Context context,
                                           @NonNull String title,
                                           @NonNull String hint,
                                           @NonNull String positiveText,
                                           @NonNull TextInputCallback onSubmit) {
        final EditText textInput = new EditText(context);
        textInput.setHint(hint);
        textInput.setInputType(InputType.TYPE_CLASS_TEXT);
        
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setView(textInput)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(context.getString(R.string.cancel), (d, which) -> d.cancel())
            .create();
        
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String text = textInput.getText().toString().trim();
            onSubmit.onTextSubmitted(text, dialog);
        });
    }
    
    // ============================================
    // MESSAGE DIALOGS
    // ============================================
    
    /**
     * Shows a simple message dialog with an OK button.
     * 
     * @param context the context
     * @param title the dialog title (can be null)
     * @param message the message to display
     */
    public static void showMessage(@NonNull Context context,
                                   @Nullable String title,
                                   @NonNull String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        if (title != null) {
            builder.setTitle(title);
        }
        
        builder.setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show();
    }
    
    /**
     * Shows an error dialog.
     * 
     * @param context the context
     * @param message the error message
     */
    public static void showError(@NonNull Context context, @NonNull String message) {
        showMessage(context, context.getString(R.string.error), message);
    }
    
    // ============================================
    // REFRESH CONFIRMATION DIALOG
    // ============================================
    
    /**
     * Shows a styled refresh confirmation dialog.
     * 
     * @param context the context
     * @param onConfirm callback when refresh is confirmed
     * @param onCancel callback when refresh is cancelled
     */
    public static void showRefreshConfirmation(@NonNull Context context,
                                               @NonNull Runnable onConfirm,
                                               @NonNull Runnable onCancel) {
        TextView tv = new TextView(context);
        tv.setTextColor(ContextCompat.getColor(context, R.color.colorLoginBackground));
        tv.setText(R.string.are_you_sure_you_want_to_refresh);
        tv.setPadding(40, 40, 40, 0);
        
        new AlertDialog.Builder(context)
            .setCustomTitle(tv)
            .setCancelable(false)
            .setNegativeButton(context.getString(R.string.yes), (dialog, id) -> onConfirm.run())
            .setPositiveButton(context.getString(R.string.no), (dialog, id) -> onCancel.run())
            .show();
    }
    
    // ============================================
    // CALLBACKS
    // ============================================
    
    /**
     * Callback interface for email input dialogs.
     */
    public interface EmailInputCallback {
        /**
         * Called when email is submitted.
         * @param email the entered email
         * @param dialog the dialog (can be dismissed if validation passes)
         */
        void onEmailSubmitted(@NonNull String email, @NonNull AlertDialog dialog);
    }
    
    /**
     * Callback interface for text input dialogs.
     */
    public interface TextInputCallback {
        /**
         * Called when text is submitted.
         * @param text the entered text
         * @param dialog the dialog (can be dismissed if validation passes)
         */
        void onTextSubmitted(@NonNull String text, @NonNull AlertDialog dialog);
    }
}
