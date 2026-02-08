package com.brosh.finance.monthlybudgetsync.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Utility class for keyboard management.
 * Provides methods to show, hide, and manage the soft keyboard.
 */
public final class KeyboardUtil {
    
    private KeyboardUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    // ============================================
    // HIDE KEYBOARD
    // ============================================
    
    /**
     * Hides the soft keyboard.
     * 
     * @param activity the current activity
     */
    public static void hideKeyboard(@NonNull Activity activity) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            hideKeyboard(activity, currentFocus);
        } else {
            // No focused view, try to hide from the decor view
            View decorView = activity.getWindow().getDecorView();
            hideKeyboard(activity, decorView);
        }
    }
    
    /**
     * Hides the soft keyboard from a specific view.
     * 
     * @param context the context
     * @param view the view that currently has focus
     */
    public static void hideKeyboard(@NonNull Context context, @NonNull View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    
    /**
     * Hides the soft keyboard from a fragment.
     * 
     * @param fragment the fragment
     */
    public static void hideKeyboard(@NonNull Fragment fragment) {
        Activity activity = fragment.getActivity();
        View view = fragment.getView();
        
        if (activity != null && view != null) {
            hideKeyboard(activity, view);
        }
    }
    
    // ============================================
    // SHOW KEYBOARD
    // ============================================
    
    /**
     * Shows the soft keyboard and focuses on the specified view.
     * 
     * @param context the context
     * @param view the view to focus
     */
    public static void showKeyboard(@NonNull Context context, @NonNull View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
    
    /**
     * Shows the soft keyboard for an EditText.
     * 
     * @param context the context
     * @param editText the EditText to focus
     */
    public static void showKeyboardForEditText(@NonNull Context context, @NonNull EditText editText) {
        editText.requestFocus();
        editText.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }
    
    /**
     * Forces the keyboard to show immediately.
     * 
     * @param context the context
     * @param view the view to focus
     */
    public static void forceShowKeyboard(@NonNull Context context, @NonNull View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }
    
    // ============================================
    // TOGGLE KEYBOARD
    // ============================================
    
    /**
     * Toggles the soft keyboard visibility.
     * 
     * @param context the context
     */
    public static void toggleKeyboard(@NonNull Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }
    
    // ============================================
    // KEYBOARD STATE
    // ============================================
    
    /**
     * Checks if the keyboard is currently visible.
     * Note: This is a heuristic and may not be 100% accurate.
     * 
     * @param activity the activity
     * @return true if keyboard appears to be visible
     */
    public static boolean isKeyboardVisible(@NonNull Activity activity) {
        View rootView = activity.getWindow().getDecorView().getRootView();
        int[] location = new int[2];
        rootView.getLocationOnScreen(location);
        
        int screenHeight = rootView.getRootView().getHeight();
        int viewHeight = rootView.getHeight();
        
        // If the visible height is significantly less than screen height,
        // the keyboard is likely showing
        int heightDiff = screenHeight - viewHeight;
        return heightDiff > screenHeight * 0.15; // 15% threshold
    }
    
    // ============================================
    // CLEAR FOCUS
    // ============================================
    
    /**
     * Clears focus from the currently focused view and hides keyboard.
     * 
     * @param activity the activity
     */
    public static void clearFocusAndHideKeyboard(@NonNull Activity activity) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            hideKeyboard(activity, currentFocus);
        }
    }
    
    /**
     * Clears focus from a specific view and hides keyboard.
     * 
     * @param context the context
     * @param view the view to clear focus from
     */
    public static void clearFocusAndHideKeyboard(@NonNull Context context, @NonNull View view) {
        view.clearFocus();
        hideKeyboard(context, view);
    }
}
