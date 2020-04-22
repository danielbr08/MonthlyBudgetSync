package com.brosh.finance.monthlybudgetsync.objects;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.services.UIService;

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
        this.category.setText(category.getName());
        this.budget.setText(String.valueOf(category.getBudget()));
        this.balance.setText(String.valueOf(category.getBalance()));

        DecimalFormat decim = new DecimalFormat("#,###.##");
        this.balance.setText(decim.format(category.getBalance()));
        this.budget.setText(decim.format(category.getBudget()));

        if (category.getBalance() < 0) {
            this.category.setTextColor(Color.RED);
            this.budget.setTextColor(Color.RED);
            this.balance.setTextColor(Color.RED);
        }
        if (category.getId() == null)
            UIService.setTotalBudgetRow(Arrays.asList(this.category, this.budget, this.balance));
    }
}
