package com.brosh.finance.monthlybudgetsync.objects;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public final class ChildEventListenerMap {

    private static ChildEventListenerMap instance;
    private static Map<DatabaseReference, ChildEventListener> childEventListenersHM;

    private ChildEventListenerMap() {
        childEventListenersHM = new HashMap<>();
    }

    public static ChildEventListenerMap getInstance() {
        if (instance == null) {
            instance = new ChildEventListenerMap();
        }
        return instance;
    }

    public Map<DatabaseReference, ChildEventListener> getChildEventListenersHM() {
        return childEventListenersHM;
    }

    public boolean isEventAlreadyExists(DatabaseReference databaseReference) {
        return childEventListenersHM.containsKey(databaseReference);
    }
}
