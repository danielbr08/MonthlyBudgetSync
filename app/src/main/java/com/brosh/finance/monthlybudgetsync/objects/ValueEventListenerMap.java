package com.brosh.finance.monthlybudgetsync.objects;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public final class ValueEventListenerMap {

    private static ValueEventListenerMap instance;
    private static Map<DatabaseReference, ValueEventListener> valueEventListenerMap;

    private ValueEventListenerMap() {
        valueEventListenerMap = new HashMap<>();
    }

    public static ValueEventListenerMap getInstance() {
        if (instance == null) {
            instance = new ValueEventListenerMap();
        }
        return instance;
    }

    public Map<DatabaseReference, ValueEventListener> getValueEventListenerHM() {
        return valueEventListenerMap;
    }

    public boolean isEventAlreadyExists(DatabaseReference databaseReference) {
        return valueEventListenerMap.containsKey(databaseReference);
    }
}

