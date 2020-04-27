package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Budget;

import java.util.List;

public class CreateBudgetViewAdapter extends RecyclerView.Adapter<CreateBudgetViewHolder> {

    private LayoutInflater mInflater;
    private List<Budget> budgets;
    private Context context;

    public CreateBudgetViewAdapter(Context context, List<Budget> budgets) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.budgets = budgets;
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public CreateBudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = this.mInflater.inflate(R.layout.create_budget_row, parent, false);
        return new CreateBudgetViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull CreateBudgetViewHolder holder, int position) {
        holder.onBindViewHolder(budgets.get(position), position);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }
}
