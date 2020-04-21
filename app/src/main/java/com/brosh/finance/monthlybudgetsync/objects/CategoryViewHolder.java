package com.brosh.finance.monthlybudgetsync.objects;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;

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
    }
}
