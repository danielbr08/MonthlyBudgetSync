package com.brosh.finance.monthlybudgetsync.ui;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.adapters.CategoriesViewAdapter;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.FormatUtil;

import java.util.List;

/**
 * Activity for displaying budget categories and their balances.
 * Extends BaseActivity for common functionality.
 */
public class BudgetActivity extends BaseActivity {
    
    private CategoriesViewAdapter adapter;
    private RecyclerView categoriesRecyclerView;
    private TextView totalBalanceTV;
    private TextView totalBudgetTV;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_budget;
    }

    @Override
    protected void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initializeViews();
        
        if (isDataValid()) {
            setCategoriesInGui();
        }
        
        setupRefreshLayout(R.id.refresh_layout_budgets, this::setCategoriesInGui);
    }
    
    /**
     * Initializes view references.
     */
    private void initializeViews() {
        categoriesRecyclerView = findViewById(R.id.categories_rows);
        totalBalanceTV = findViewById(R.id.totalBalance);
        totalBudgetTV = findViewById(R.id.totalBudget);
    }

    /**
     * Populates the RecyclerView with category data and updates totals.
     */
    public void setCategoriesInGui() {
        if (!isDataValid()) {
            return;
        }
        
        String currentRefMonth = DateUtil.getYearMonth(month.getRefMonth(), Config.SEPARATOR);
        List<Category> categories = dbUtil.getCategoriesByPriority(currentRefMonth);

        // Calculate totals
        int totalBudget = 0;
        double totalBalance = 0;

        for (Category category : categories) {
            totalBudget += category.getBudget();
            totalBalance += category.getBalance();
        }
        
        // Update total labels using FormatUtil
        String currency = user.getUserSettings().getCurrency();
        totalBalance = FormatUtil.roundToTwoDecimals(totalBalance);
        
        totalBalanceTV.setText(FormatUtil.formatCurrency(totalBalance, currency));
        totalBudgetTV.setText(FormatUtil.formatCurrency(totalBudget, currency));
        
        // Color-code balance based on value
        if (totalBalance < 0) {
            totalBalanceTV.setTextColor(0xFFEF4444); // Red for negative
        } else {
            totalBalanceTV.setTextColor(0xFF10B981); // Green for positive
        }
        
        // Setup RecyclerView
        if (adapter == null) {
            adapter = new CategoriesViewAdapter(this, categories);
            categoriesRecyclerView.setAdapter(adapter);
            categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            adapter.updateData(categories);
        }
    }
}
