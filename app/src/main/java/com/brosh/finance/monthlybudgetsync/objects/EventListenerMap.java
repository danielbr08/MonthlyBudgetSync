package com.brosh.finance.monthlybudgetsync.objects;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventListenerMap {

    private static EventListenerMap instance;
    private static Map<DatabaseReference, ChildEventListener> childEventListenersHM;

    private EventListenerMap() {
        childEventListenersHM = new HashMap<>();
    }

    public static EventListenerMap getInstance() {
        if (instance == null) {
            instance = new EventListenerMap();
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
