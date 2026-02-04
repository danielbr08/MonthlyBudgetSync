package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Budget;

import java.util.ArrayList;
import java.util.List;

public class CreateBudgetViewAdapter extends RecyclerView.Adapter<CreateBudgetViewHolder> {

    private final LayoutInflater mInflater;
    private final List<Budget> budgets;
    private final Context context;

    public CreateBudgetViewAdapter(Context context, List<Budget> budgets) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public CreateBudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = this.mInflater.inflate(R.layout.create_budget_row, parent, false);

        CreateBudgetViewHolder holder = new CreateBudgetViewHolder(view);
        holder.itemView.setOnLongClickListener(v -> {
            LinearLayout layout = (LinearLayout) v;
            String category = ((EditText) layout.getChildAt(0)).getText().toString().trim();
            String value = ((EditText) layout.getChildAt(1)).getText().toString().trim().replace(Definitions.COMMA, "");
            boolean constPayment = ((CheckBox) layout.getChildAt(2)).isChecked();
            String shop = ((EditText) layout.getChildAt(3)).getText().toString().trim();
            int chargeDay = Integer.parseInt(((TextView) layout.getChildAt(4)).getText().toString().trim());

            int val = !value.isEmpty() ? Integer.parseInt(value) : 0;
            int index = holder.getBindingAdapterPosition();
            if (index != RecyclerView.NO_POSITION) {
                budgets.add(index + 1, new Budget(category, val, constPayment, shop, chargeDay, budgets.size() + 1));
                notifyItemInserted(index + 1);
            }
            return true;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CreateBudgetViewHolder holder, int position) {
        holder.onBindViewHolder(budgets.get(position), position, context);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }
}
