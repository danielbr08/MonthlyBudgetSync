package com.brosh.finance.monthlybudgetsync.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.objects.User;
import com.brosh.finance.monthlybudgetsync.utils.DBUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

/**
 * Base activity class providing common functionality for all activities.
 * Reduces code duplication for:
 * - Ad setup
 * - Toolbar setup
 * - User/Month initialization
 * - Refresh layout handling
 */
public abstract class BaseActivity extends AppCompatActivity {
    
    protected DBUtil dbUtil;
    @Nullable protected User user;
    @Nullable protected Month month;
    @Nullable protected String refMonth;
    @Nullable protected SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        
        // Initialize common components
        initializeData();
        setupAds();
        setupToolbar();
        
        // Call child-specific initialization
        onActivityCreated(savedInstanceState);
    }
    
    /**
     * Returns the layout resource ID for this activity.
     * Subclasses must override this to provide their layout.
     */
    @LayoutRes
    protected abstract int getLayoutResourceId();
    
    /**
     * Called after base initialization is complete.
     * Subclasses should override this for activity-specific setup.
     */
    protected abstract void onActivityCreated(@Nullable Bundle savedInstanceState);
    
    /**
     * Initializes common data (DBUtil, User, Month).
     * Override to customize data initialization.
     */
    protected void initializeData() {
        dbUtil = DBUtil.getInstance();
        user = dbUtil.getUser();
        
        Bundle extras = getIntent().getExtras();
        refMonth = extras != null ? extras.getString(Definitions.MONTH, null) : null;
        
        if (refMonth != null) {
            month = dbUtil.getMonth(refMonth);
        }
    }
    
    /**
     * Sets up advertisement visibility based on user settings.
     * Override to customize ad behavior.
     */
    protected void setupAds() {
        boolean adEnabled = user != null && user.getUserSettings().isAdEnabled();
        View adView = findViewById(R.id.adView);
        
        if (adEnabled) {
            UiUtil.addAdvertiseToActivity(this);
        } else if (adView != null) {
            adView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Sets up the toolbar with the current month.
     * Override to customize toolbar behavior.
     */
    protected void setupToolbar() {
        String yearMonth = month != null ? month.getYearMonth() : null;
        UiUtil.setToolbar(this, yearMonth);
    }
    
    /**
     * Sets up the refresh layout with the specified listener.
     * Call this in onActivityCreated if refresh is needed.
     *
     * @param refreshLayoutId Resource ID of the SwipeRefreshLayout
     * @param onRefresh Action to perform on refresh
     */
    protected void setupRefreshLayout(int refreshLayoutId, Runnable onRefresh) {
        refreshLayout = findViewById(refreshLayoutId);
        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(() -> {
                if (onRefresh != null) {
                    onRefresh.run();
                }
                refreshLayout.setRefreshing(false);
            });
        }
    }
    
    /**
     * Checks if data is valid (user and month exist).
     */
    protected boolean isDataValid() {
        return user != null && month != null;
    }
    
    /**
     * Returns the current user.
     */
    @Nullable
    public User getUser() {
        return user;
    }
    
    /**
     * Returns the current month.
     */
    @Nullable
    public Month getMonth() {
        return month;
    }
    
    /**
     * Returns the DBUtil instance.
     */
    public DBUtil getDbUtil() {
        return dbUtil;
    }
}
