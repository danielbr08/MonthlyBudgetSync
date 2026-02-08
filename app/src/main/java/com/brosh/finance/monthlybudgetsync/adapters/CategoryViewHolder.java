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
        categoryTV.setText(category.getName());
        balanceTV.setText(FormatUtil.formatBalance(category.getBalance()));
        budgetTV.setText(FormatUtil.formatBudget(category.getBudget()));

        // Style based on balance (red if negative)
        if (category.getBalance() < 0) {
            UiUtil.setTextViewColor(allTextViews, Color.RED);
        }
        
        // Style total row differently (identified by null ID)
        if (category.getId() == null) {
            UiUtil.setTotalBudgetRow(allTextViews);
        }
    }
}
