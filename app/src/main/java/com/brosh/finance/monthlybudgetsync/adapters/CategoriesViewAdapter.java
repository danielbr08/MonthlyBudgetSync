package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Category;

import java.util.List;

public class CategoriesViewAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
    private LayoutInflater mInflater;
    private List<Category> categories;

    public CategoriesViewAdapter(Context context, List<Category> categories) {
        this.categories = categories;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.category_row_item, parent, false);
        return new CategoryViewHolder(view);
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
