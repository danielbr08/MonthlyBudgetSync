package com.brosh.finance.monthlybudgetsync.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.brosh.finance.monthlybudgetsync.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
        new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
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
        new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
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
    // STYLED EMAIL INPUT DIALOG
    // ============================================
    
    /**
     * Shows a beautifully styled email input dialog.
     * Uses the custom dialog_email_input.xml layout.
     * 
     * @param context the context
     * @param title the dialog title
     * @param subtitle the dialog subtitle/hint
     * @param positiveText positive button text
     * @param onSubmit callback with the entered email when submitted
     */
    public static void showEmailInputDialog(@NonNull Context context,
                                            @NonNull String title,
                                            @NonNull String subtitle,
                                            @NonNull String positiveText,
                                            @NonNull EmailInputCallback onSubmit) {
        // Create custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_email_input);
        
        // Make dialog background transparent to show our rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
        
        // Get views
        TextView titleView = dialog.findViewById(R.id.dialog_title);
        TextView subtitleView = dialog.findViewById(R.id.dialog_subtitle);
        TextInputLayout emailInputLayout = dialog.findViewById(R.id.email_input_layout);
        TextInputEditText emailInput = dialog.findViewById(R.id.email_input);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnSubmit = dialog.findViewById(R.id.btn_submit);
        
        // Set content
        titleView.setText(title);
        subtitleView.setText(subtitle);
        btnSubmit.setText(positiveText);
        
        // Cancel button click
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // Submit button click
        btnSubmit.setOnClickListener(v -> {
            String email = emailInput.getText() != null ? 
                emailInput.getText().toString().trim() : "";
            
            if (!ValidationUtil.isValidEmail(email)) {
                emailInputLayout.setError(context.getString(R.string.invalid_email));
                return;
            }
            
            emailInputLayout.setError(null);
            onSubmit.onEmailSubmitted(email, dialog);
        });
        
        dialog.setCancelable(true);
        dialog.show();
    }
    
    /**
     * Shows a beautifully styled share dialog specifically.
     * Convenience method for share functionality.
     * 
     * @param context the context
     * @param onShare callback when share is submitted
     */
    public static void showShareDialog(@NonNull Context context,
                                       @NonNull ShareCallback onShare) {
        showEmailInputDialog(
            context,
            context.getString(R.string.share_budget),
            context.getString(R.string.please_enter_user_email_to_share),
            context.getString(R.string.share),
            (email, dialog) -> onShare.onShare(email, dialog)
        );
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
        // Create custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_email_input);
        
        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
        
        // Get views
        TextView titleView = dialog.findViewById(R.id.dialog_title);
        TextView subtitleView = dialog.findViewById(R.id.dialog_subtitle);
        TextInputLayout inputLayout = dialog.findViewById(R.id.email_input_layout);
        TextInputEditText textInput = dialog.findViewById(R.id.email_input);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnSubmit = dialog.findViewById(R.id.btn_submit);
        
        // Configure for text input
        titleView.setText(title);
        subtitleView.setText(hint);
        btnSubmit.setText(positiveText);
        inputLayout.setHint(context.getString(R.string.enter_value));
        inputLayout.setStartIconDrawable(null);
        textInput.setInputType(InputType.TYPE_CLASS_TEXT);
        
        // Hide share icon for text input
        btnSubmit.setIcon(null);
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSubmit.setOnClickListener(v -> {
            String text = textInput.getText() != null ? 
                textInput.getText().toString().trim() : "";
            onSubmit.onTextSubmitted(text, dialog);
        });
        
        dialog.setCancelable(true);
        dialog.show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        
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
        new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
            .setTitle(R.string.refresh)
            .setMessage(R.string.are_you_sure_you_want_to_refresh)
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
        void onEmailSubmitted(@NonNull String email, @NonNull Dialog dialog);
    }
    
    /**
     * Callback interface for share dialogs.
     */
    public interface ShareCallback {
        /**
         * Called when share is submitted.
         * @param email the entered email
         * @param dialog the dialog (can be dismissed if validation passes)
         */
        void onShare(@NonNull String email, @NonNull Dialog dialog);
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
        void onTextSubmitted(@NonNull String text, @NonNull Dialog dialog);
    }
}
