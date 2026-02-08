package com.brosh.finance.monthlybudgetsync.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

/**
 * Base implementation of ChildEventListener with default no-op methods.
 * Reduces boilerplate code by providing empty default implementations.
 * Subclasses only need to override the methods they care about.
 */
public abstract class BaseChildEventListener implements ChildEventListener {
    
    private static final String TAG = "BaseChildEventListener";
    
    private final String listenerName;
    
    /**
     * Creates a new BaseChildEventListener.
     * 
     * @param listenerName descriptive name for logging purposes
     */
    protected BaseChildEventListener(@NonNull String listenerName) {
        this.listenerName = listenerName;
    }
    
    /**
     * Creates a new BaseChildEventListener with default name.
     */
    protected BaseChildEventListener() {
        this.listenerName = getClass().getSimpleName();
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        // Default no-op implementation
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        // Default no-op implementation
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
        // Default no-op implementation
    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        // Default no-op implementation
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
}
