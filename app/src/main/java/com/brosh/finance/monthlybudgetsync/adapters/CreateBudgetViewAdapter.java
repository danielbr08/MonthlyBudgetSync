package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
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

        CreateBudgetViewHolder holder = new CreateBudgetViewHolder(view);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int i = 0;
                String category = ((EditText) ((LinearLayout) v).getChildAt(i++)).getText().toString().trim();
                String value = ((EditText) ((LinearLayout) v).getChildAt(i++)).getText().toString().trim().replace(Definitions.COMMA, "");
                boolean constPayment = ((CheckBox) ((LinearLayout) v).getChildAt(i++)).isChecked();
                String shop = ((EditText) ((LinearLayout) v).getChildAt(i++)).getText().toString().trim();
                int chargeDay = Integer.valueOf(((TextView) ((LinearLayout) v).getChildAt(i)).getText().toString().trim());

                int val = value != "" ? Integer.valueOf(value) : 0;
                int index = holder.getPosition();
                budgets.add(index + 1, new Budget(category, val, constPayment, shop, chargeDay, budgets.size() + 1));
                notifyItemInserted(index + 1);
                return true;
            }
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
