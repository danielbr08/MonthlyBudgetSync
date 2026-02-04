package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.ui.BudgetActivity;
import com.brosh.finance.monthlybudgetsync.ui.TransactionsActivity;

import java.util.List;

public class CategoriesViewAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

    private final LayoutInflater mInflater;
    private final List<Category> categories;
    private final Context context;

    public CategoriesViewAdapter(Context context, List<Category> categories) {
        this.categories = categories != null ? categories : new java.util.ArrayList<>();
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.category_row_item, parent, false);
        CategoryViewHolder holder = new CategoryViewHolder(view);
        holder.itemView.setOnLongClickListener(v -> {
            if (!(v instanceof LinearLayout layout)) {
                return false;
            }
            View childView = layout.getChildAt(0);
            if (!(childView instanceof TextView textView)) {
                return false;
            }
            String category = textView.getText().toString().trim();
            Intent intent = new Intent(context, TransactionsActivity.class);
            
            if (context instanceof BudgetActivity budgetActivity) {
                Month month = budgetActivity.getMonth();
                intent.putExtra("categoryName", category);
                intent.putExtra(Definitions.USER, budgetActivity.getUser());
                intent.putExtra(Definitions.MONTH, month != null ? month.getYearMonth() : null);
                context.startActivity(intent);
            }
            return true;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, int position) {
        holder.onBindViewHolder(categories.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
