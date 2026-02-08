package com.brosh.finance.monthlybudgetsync;

import android.app.Application;
import android.util.Log;

import com.brosh.finance.monthlybudgetsync.utils.PreferencesManager;
import com.brosh.finance.monthlybudgetsync.utils.ResourceHelper;

/**
 * Application class for MonthlyBudgetSync.
 * Initializes global utilities and configurations.
 */
public class MonthlyBudgetApp extends Application {
    
    private static final String TAG = "MonthlyBudgetApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        initializeUtilities();
        
        Log.d(TAG, "Application initialized");
    }
    
    /**
     * Initializes all utility classes that require application context.
     */
    private void initializeUtilities() {
        // Initialize PreferencesManager for SharedPreferences access
        PreferencesManager.initialize(this);
        
        // Initialize ResourceHelper for resource access
        ResourceHelper.initialize(this);
    }
}
