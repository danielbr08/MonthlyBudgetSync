package com.brosh.finance.monthlybudgetsync.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

/**
 * Helper class for accessing resources.
 * Provides centralized, null-safe access to app resources.
 * Must be initialized with application context before use.
 */
public final class ResourceHelper {
    
    private static WeakReference<Context> contextRef;
    
    private ResourceHelper() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    /**
     * Initializes the ResourceHelper with application context.
     * Call this once in Application.onCreate().
     * 
     * @param context the application context
     */
    public static void initialize(@NonNull Context context) {
        contextRef = new WeakReference<>(context.getApplicationContext());
    }
    
    /**
     * Gets the application context.
     * 
     * @return Context or null if not initialized
     */
    @Nullable
    public static Context getContext() {
        return contextRef != null ? contextRef.get() : null;
    }
    
    /**
     * Checks if the helper is initialized.
     * 
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return getContext() != null;
    }
    
    // ============================================
    // STRING RESOURCES
    // ============================================
    
    /**
     * Gets a string resource by ID.
     * 
     * @param resId the string resource ID
     * @return the string, or empty string if context not available
     */
    @NonNull
    public static String getString(@StringRes int resId) {
        Context ctx = getContext();
        return ctx != null ? ctx.getString(resId) : "";
    }
    
    /**
     * Gets a formatted string resource.
     * 
     * @param resId the string resource ID
     * @param formatArgs format arguments
     * @return the formatted string, or empty string if context not available
     */
    @NonNull
    public static String getString(@StringRes int resId, Object... formatArgs) {
        Context ctx = getContext();
        return ctx != null ? ctx.getString(resId, formatArgs) : "";
    }
    
    /**
     * Gets a string resource or a default value if unavailable.
     * 
     * @param resId the string resource ID
     * @param defaultValue the default value if context not available
     * @return the string or default value
     */
    @NonNull
    public static String getStringOrDefault(@StringRes int resId, @NonNull String defaultValue) {
        Context ctx = getContext();
        return ctx != null ? ctx.getString(resId) : defaultValue;
    }
    
    // ============================================
    // STRING ARRAY RESOURCES
    // ============================================
    
    /**
     * Gets a string array resource by ID.
     * 
     * @param resId the string array resource ID
     * @return the string array, or empty array if context not available
     */
    @NonNull
    public static String[] getStringArray(@ArrayRes int resId) {
        Context ctx = getContext();
        return ctx != null ? ctx.getResources().getStringArray(resId) : new String[0];
    }
    
    // ============================================
    // COLOR RESOURCES
    // ============================================
    
    /**
     * Gets a color resource by ID.
     * 
     * @param resId the color resource ID
     * @return the color int, or 0 if context not available
     */
    @ColorInt
    public static int getColor(@ColorRes int resId) {
        Context ctx = getContext();
        return ctx != null ? ContextCompat.getColor(ctx, resId) : 0;
    }
    
    // ============================================
    // DRAWABLE RESOURCES
    // ============================================
    
    /**
     * Gets a drawable resource by ID.
     * 
     * @param resId the drawable resource ID
     * @return the Drawable, or null if context not available
     */
    @Nullable
    public static Drawable getDrawable(@DrawableRes int resId) {
        Context ctx = getContext();
        return ctx != null ? ContextCompat.getDrawable(ctx, resId) : null;
    }
    
    // ============================================
    // DIMENSION RESOURCES
    // ============================================
    
    /**
     * Gets a dimension resource by ID.
     * 
     * @param resId the dimension resource ID
     * @return the dimension in pixels, or 0 if context not available
     */
    public static float getDimension(@DimenRes int resId) {
        Context ctx = getContext();
        return ctx != null ? ctx.getResources().getDimension(resId) : 0f;
    }
    
    /**
     * Gets a dimension resource by ID as an integer (rounded).
     * 
     * @param resId the dimension resource ID
     * @return the dimension in pixels (rounded), or 0 if context not available
     */
    public static int getDimensionPixelSize(@DimenRes int resId) {
        Context ctx = getContext();
        return ctx != null ? ctx.getResources().getDimensionPixelSize(resId) : 0;
    }
    
    // ============================================
    // INTEGER RESOURCES
    // ============================================
    
    /**
     * Gets an integer resource by ID.
     * 
     * @param resId the integer resource ID
     * @return the integer, or 0 if context not available
     */
    public static int getInteger(int resId) {
        Context ctx = getContext();
        return ctx != null ? ctx.getResources().getInteger(resId) : 0;
    }
    
    // ============================================
    // BOOLEAN RESOURCES
    // ============================================
    
    /**
     * Gets a boolean resource by ID.
     * 
     * @param resId the boolean resource ID
     * @return the boolean, or false if context not available
     */
    public static boolean getBoolean(int resId) {
        Context ctx = getContext();
        return ctx != null && ctx.getResources().getBoolean(resId);
    }
}
