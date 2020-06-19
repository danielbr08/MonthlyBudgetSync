package com.brosh.finance.monthlybudgetsync.adapters;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.services.UiUtil;

import java.text.DecimalFormat;
import java.util.Arrays;

public class CategoryViewHolder extends RecyclerView.ViewHolder {
    public TextView category;
    public TextView budget;
    public TextView balance;

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        this.category = itemView.findViewById(R.id.categoryLabel);
        this.budget = itemView.findViewById(R.id.budgetLabel);
        this.balance = itemView.findViewById(R.id.balanceLabel);
    }

    public void onBindViewHolder(Category category) {
        DecimalFormat decim = new DecimalFormat("#,###.##");

        this.category.setText(category.getName());
        this.balance.setText(decim.format(category.getBalance()));
        this.budget.setText(decim.format(category.getBudget()));

        if (category.getBalance() < 0)
            UiUtil.setTextViewColor(Arrays.asList(this.category, this.budget, this.balance), Color.RED);
        if (category.getId() == null)
            UiUtil.setTotalBudgetRow(Arrays.asList(this.category, this.budget, this.balance));
    }
}
