package com.brosh.finance.monthlybudgetsync.adapters;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.utils.FormatUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.util.Arrays;
import java.util.List;

/**
 * ViewHolder for displaying budget categories in a RecyclerView.
 * Handles formatting and styling of category, budget, and balance values.
 */
public class CategoryViewHolder extends RecyclerView.ViewHolder {

    // ============================================
    // COLORS
    // ============================================
    
    private static final int COLOR_WHITE = Color.WHITE;
    private static final int COLOR_BUDGET = 0xFFB8C5D6; // Light gray-blue
    private static final int COLOR_POSITIVE = 0xFF10B981; // Green
    private static final int COLOR_NEGATIVE = 0xFFEF4444; // Red

    // ============================================
    // UI COMPONENTS
    // ============================================

    private final TextView categoryTV;
    private final TextView budgetTV;
    private final TextView balanceTV;
    private final List<TextView> allTextViews;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        this.categoryTV = itemView.findViewById(R.id.categoryLabel);
        this.budgetTV = itemView.findViewById(R.id.budgetLabel);
        this.balanceTV = itemView.findViewById(R.id.balanceLabel);
        this.allTextViews = Arrays.asList(categoryTV, budgetTV, balanceTV);
    }

    // ============================================
    // DATA BINDING
    // ============================================

    /**
     * Binds category data to the ViewHolder.
     * Uses FormatUtil for consistent number formatting.
     *
     * @param category the category data to display
     */
    public void onBindViewHolder(@NonNull Category category) {
        // Reset colors first (for recycled views)
        categoryTV.setTextColor(COLOR_WHITE);
        budgetTV.setTextColor(COLOR_BUDGET);
        
        // Set text values
        categoryTV.setText(category.getName());
        balanceTV.setText(FormatUtil.formatBalance(category.getBalance()));
        budgetTV.setText(FormatUtil.formatBudget(category.getBudget()));

        // Style balance based on value (green if positive, red if negative)
        if (category.getBalance() < 0) {
            balanceTV.setTextColor(COLOR_NEGATIVE);
        } else {
            balanceTV.setTextColor(COLOR_POSITIVE);
        }

        // Style total row differently (identified by null ID)
        if (category.getId() == null) {
            UiUtil.setTotalBudgetRow(allTextViews);
        }
    }
}
