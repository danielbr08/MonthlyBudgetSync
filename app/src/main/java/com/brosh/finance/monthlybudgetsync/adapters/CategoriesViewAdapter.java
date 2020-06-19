package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.ui.BudgetActivity;
import com.brosh.finance.monthlybudgetsync.ui.TransactionsActivity;

import java.util.List;

public class CategoriesViewAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
    private LayoutInflater mInflater;
    private List<Category> categories;
    private Context context;

    public CategoriesViewAdapter(Context context, List<Category> categories) {
        this.categories = categories;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.category_row_item, parent, false);
        CategoryViewHolder holder = new CategoryViewHolder(view);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String category = ((TextView) ((LinearLayout) v).getChildAt(0)).getText().toString().trim();
                Intent intent = new Intent(context, TransactionsActivity.class);
                Month month = ((BudgetActivity) context).getMonth();
                intent.putExtra("categoryName", category);
                intent.putExtra(Definitions.USER, ((BudgetActivity) context).getUser());
                intent.putExtra(Definitions.MONTH, month == null ? month : month.getYearMonth());
                context.startActivity(intent);
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, int position) {
        holder.onBindViewHolder(categories.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
