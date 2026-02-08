package com.brosh.finance.monthlybudgetsync.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.objects.ChildEventListenerMap;
import com.brosh.finance.monthlybudgetsync.objects.ValueEventListenerMap;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * Manages Firebase database listeners lifecycle.
 * Provides helper methods for adding, removing, and tracking listeners
 * to prevent memory leaks and duplicate listeners.
 */
public final class ListenerManager {
    
    private static final String TAG = "ListenerManager";
    
    private ListenerManager() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    // ============================================
    // VALUE EVENT LISTENERS
    // ============================================
    
    /**
     * Adds a ValueEventListener if not already registered.
     * 
     * @param reference the database reference
     * @param listener the listener to add
     * @return true if listener was added, false if already exists
     */
    public static boolean addValueEventListener(@NonNull DatabaseReference reference, 
                                                 @NonNull ValueEventListener listener) {
        if (ValueEventListenerMap.getInstance().isEventAlreadyExists(reference)) {
            return false;
        }
        
        reference.addValueEventListener(listener);
        ValueEventListenerMap.getInstance().getValueEventListenerHM().put(reference, listener);
        return true;
    }
    
    /**
     * Adds a single ValueEventListener (auto-removed after first callback).
     * 
     * @param reference the database reference
     * @param listener the listener to add
     */
    public static void addSingleValueEventListener(@NonNull DatabaseReference reference,
                                                   @NonNull ValueEventListener listener) {
        reference.addListenerForSingleValueEvent(listener);
    }
    
    /**
     * Removes a ValueEventListener if registered.
     * 
     * @param reference the database reference
     * @return true if listener was removed, false if not found
     */
    public static boolean removeValueEventListener(@NonNull DatabaseReference reference) {
        Map<DatabaseReference, ValueEventListener> listeners = 
            ValueEventListenerMap.getInstance().getValueEventListenerHM();
        
        ValueEventListener listener = listeners.get(reference);
        if (listener == null) {
            return false;
        }
        
        reference.removeEventListener(listener);
        listeners.remove(reference);
        return true;
    }
    
    // ============================================
    // CHILD EVENT LISTENERS
    // ============================================
    
    /**
     * Adds a ChildEventListener if not already registered.
     * 
     * @param reference the database reference
     * @param listener the listener to add
     * @return true if listener was added, false if already exists
     */
    public static boolean addChildEventListener(@NonNull DatabaseReference reference,
                                                @NonNull ChildEventListener listener) {
        if (ChildEventListenerMap.getInstance().isEventAlreadyExists(reference)) {
            return false;
        }
        
        reference.addChildEventListener(listener);
        ChildEventListenerMap.getInstance().getChildEventListenersHM().put(reference, listener);
        return true;
    }
    
    /**
     * Removes a ChildEventListener if registered.
     * 
     * @param reference the database reference
     * @return true if listener was removed, false if not found
     */
    public static boolean removeChildEventListener(@NonNull DatabaseReference reference) {
        Map<DatabaseReference, ChildEventListener> listeners = 
            ChildEventListenerMap.getInstance().getChildEventListenersHM();
        
        ChildEventListener listener = listeners.get(reference);
        if (listener == null) {
            return false;
        }
        
        reference.removeEventListener(listener);
        listeners.remove(reference);
        return true;
    }
    
    // ============================================
    // BULK OPERATIONS
    // ============================================
    
    /**
     * Removes all listeners registered under a path (including child paths).
     * 
     * @param reference the parent database reference
     */
    public static void removeAllListenersUnderPath(@NonNull DatabaseReference reference) {
        String path = reference.toString();
        
        // Remove ValueEventListeners
        Map<DatabaseReference, ValueEventListener> valueListeners = 
            ValueEventListenerMap.getInstance().getValueEventListenerHM();
        valueListeners.entrySet().removeIf(entry -> {
            if (entry.getKey().toString().startsWith(path)) {
                entry.getKey().removeEventListener(entry.getValue());
                return true;
            }
            return false;
        });
        
        // Remove ChildEventListeners
        Map<DatabaseReference, ChildEventListener> childListeners = 
            ChildEventListenerMap.getInstance().getChildEventListenersHM();
        childListeners.entrySet().removeIf(entry -> {
            if (entry.getKey().toString().startsWith(path)) {
                entry.getKey().removeEventListener(entry.getValue());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Removes all registered listeners.
     * Call this on logout or app termination.
     */
    public static void removeAllListeners() {
        // Remove all ValueEventListeners
        Map<DatabaseReference, ValueEventListener> valueListeners = 
            ValueEventListenerMap.getInstance().getValueEventListenerHM();
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueListeners.entrySet()) {
            try {
                entry.getKey().removeEventListener(entry.getValue());
            } catch (Exception e) {
                Log.w(TAG, "Error removing ValueEventListener: " + e.getMessage());
            }
        }
        valueListeners.clear();
        
        // Remove all ChildEventListeners
        Map<DatabaseReference, ChildEventListener> childListeners = 
            ChildEventListenerMap.getInstance().getChildEventListenersHM();
        for (Map.Entry<DatabaseReference, ChildEventListener> entry : childListeners.entrySet()) {
            try {
                entry.getKey().removeEventListener(entry.getValue());
            } catch (Exception e) {
                Log.w(TAG, "Error removing ChildEventListener: " + e.getMessage());
            }
        }
        childListeners.clear();
    }
    
    // ============================================
    // UTILITY METHODS
    // ============================================
    
    /**
     * Checks if any listener is registered for a reference.
     * 
     * @param reference the database reference
     * @return true if a listener exists
     */
    public static boolean hasListener(@NonNull DatabaseReference reference) {
        return ValueEventListenerMap.getInstance().isEventAlreadyExists(reference) ||
               ChildEventListenerMap.getInstance().isEventAlreadyExists(reference);
    }
    
    /**
     * Returns the count of all registered listeners.
     * 
     * @return total listener count
     */
    public static int getListenerCount() {
        return ValueEventListenerMap.getInstance().getValueEventListenerHM().size() +
               ChildEventListenerMap.getInstance().getChildEventListenersHM().size();
    }
    
    // ============================================
    // SIMPLE LISTENER FACTORIES
    // ============================================
    
    /**
     * Creates a simple ValueEventListener with just onDataChange callback.
     * Provides default empty implementation for onCancelled.
     * 
     * @param onDataChange callback for data changes
     * @return ValueEventListener instance
     */
    @NonNull
    public static ValueEventListener createValueListener(@NonNull DataChangeCallback onDataChange) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onDataChange.onDataChange(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Listener cancelled: " + databaseError.getMessage());
            }
        };
    }
    
    /**
     * Creates a ValueEventListener with both callbacks.
     * 
     * @param onDataChange callback for data changes
     * @param onCancelled callback for cancellation
     * @return ValueEventListener instance
     */
    @NonNull
    public static ValueEventListener createValueListener(@NonNull DataChangeCallback onDataChange,
                                                         @NonNull CancelledCallback onCancelled) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onDataChange.onDataChange(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onCancelled.onCancelled(databaseError);
            }
        };
    }
    
    // ============================================
    // CALLBACK INTERFACES
    // ============================================
    
    /**
     * Callback interface for data changes.
     */
    public interface DataChangeCallback {
        void onDataChange(@NonNull DataSnapshot dataSnapshot);
    }
    
    /**
     * Callback interface for listener cancellation.
     */
    public interface CancelledCallback {
        void onCancelled(@NonNull DatabaseError databaseError);
    }
}
