package com.brosh.finance.monthlybudgetsync.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Base implementation of ValueEventListener with enhanced error handling.
 * Provides common utility methods and logging for Firebase value listeners.
 */
public abstract class BaseValueEventListener implements ValueEventListener {
    
    private static final String TAG = "BaseValueEventListener";
    
    private final String listenerName;
    
    /**
     * Creates a new BaseValueEventListener.
     * 
     * @param listenerName descriptive name for logging purposes
     */
    protected BaseValueEventListener(@NonNull String listenerName) {
        this.listenerName = listenerName;
    }
    
    /**
     * Creates a new BaseValueEventListener with default name.
     */
    protected BaseValueEventListener() {
        this.listenerName = getClass().getSimpleName();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, listenerName + " cancelled: " + error.getMessage());
    }
    
    /**
     * Helper method to safely get a key from a snapshot.
     * 
     * @param snapshot the data snapshot
     * @return the key, or null if snapshot is null
     */
    @Nullable
    protected String getKey(@Nullable DataSnapshot snapshot) {
        return snapshot != null ? snapshot.getKey() : null;
    }
    
    /**
     * Helper method to safely get a value from a snapshot.
     * 
     * @param snapshot the data snapshot
     * @param clazz the class to cast to
     * @param <T> the type
     * @return the value, or null if not found or cast fails
     */
    @Nullable
    protected <T> T getValue(@Nullable DataSnapshot snapshot, @NonNull Class<T> clazz) {
        return snapshot != null ? snapshot.getValue(clazz) : null;
    }
    
    /**
     * Helper method to get a Long value from a snapshot and convert to int.
     * 
     * @param snapshot the data snapshot
     * @param defaultValue default value if conversion fails
     * @return the int value or default
     */
    protected int getIntValue(@Nullable DataSnapshot snapshot, int defaultValue) {
        if (snapshot == null) return defaultValue;
        
        Object value = snapshot.getValue();
        if (value instanceof Long longValue) {
            return longValue.intValue();
        } else if (value instanceof Integer intValue) {
            return intValue;
        }
        return defaultValue;
    }
    
    /**
     * Helper method to get a Double value from a snapshot.
     * 
     * @param snapshot the data snapshot
     * @param defaultValue default value if conversion fails
     * @return the double value or default
     */
    protected double getDoubleValue(@Nullable DataSnapshot snapshot, double defaultValue) {
        if (snapshot == null) return defaultValue;
        
        Object value = snapshot.getValue();
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Helper method to get a Boolean value from a snapshot.
     * 
     * @param snapshot the data snapshot
     * @param defaultValue default value if conversion fails
     * @return the boolean value or default
     */
    protected boolean getBooleanValue(@Nullable DataSnapshot snapshot, boolean defaultValue) {
        if (snapshot == null) return defaultValue;
        
        Object value = snapshot.getValue();
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
