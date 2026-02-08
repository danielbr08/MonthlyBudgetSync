package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.brosh.finance.monthlybudgetsync.objects.Category;
import com.brosh.finance.monthlybudgetsync.objects.Month;
import com.brosh.finance.monthlybudgetsync.ui.BudgetActivity;
import com.brosh.finance.monthlybudgetsync.ui.TransactionsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RecyclerView adapter for displaying budget categories.
 * Uses DiffUtil for efficient updates.
 */
public class CategoriesViewAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

    // ============================================
    // CONSTANTS
    // ============================================

    private static final String EXTRA_CATEGORY_NAME = "categoryName";

    // ============================================
    // FIELDS
    // ============================================

    private final LayoutInflater mInflater;
    private final List<Category> categories;
    private final Context context;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public CategoriesViewAdapter(Context context, List<Category> categories) {
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        setHasStableIds(true);
    }

    // ============================================
    // ADAPTER METHODS
    // ============================================

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.category_row_item, parent, false);
        CategoryViewHolder holder = new CategoryViewHolder(view);
        holder.itemView.setOnLongClickListener(v -> handleLongClick(v));
        return holder;
    }

    // ============================================
    // EVENT HANDLERS
    // ============================================

    /**
     * Handles long click on a category row.
     * Opens TransactionsActivity filtered by the selected category.
     */
    private boolean handleLongClick(View v) {
        if (!(v instanceof LinearLayout layout)) {
            return false;
        }
        View childView = layout.getChildAt(0);
        if (!(childView instanceof TextView textView)) {
            return false;
        }
        
        String category = textView.getText().toString().trim();
        
        if (context instanceof BudgetActivity budgetActivity) {
            Intent intent = new Intent(context, TransactionsActivity.class);
            Month month = budgetActivity.getMonth();
            intent.putExtra(EXTRA_CATEGORY_NAME, category);
            intent.putExtra(Definitions.USER, budgetActivity.getUser());
            intent.putExtra(Definitions.MONTH, month != null ? month.getYearMonth() : null);
            context.startActivity(intent);
        }
        return true;
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, int position) {
        holder.onBindViewHolder(categories.get(position));
    }

    @Override
    public long getItemId(int position) {
        Category category = categories.get(position);
        return category.getId() != null ? category.getId().hashCode() : position;
    }

    // ============================================
    // DATA OPERATIONS
    // ============================================

    /**
     * Updates the adapter with new data using DiffUtil for efficient updates.
     *
     * @param newCategories the new list of categories
     */
    public void updateData(@NonNull List<Category> newCategories) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
            new CategoryDiffCallback(categories, newCategories)
        );
        categories.clear();
        categories.addAll(newCategories);
        diffResult.dispatchUpdatesTo(this);
    }

    // ============================================
    // INNER CLASSES
    // ============================================

    /**
     * DiffUtil callback for efficient RecyclerView updates.
     */
    private static class CategoryDiffCallback extends DiffUtil.Callback {
        private final List<Category> oldList;
        private final List<Category> newList;
        
        CategoryDiffCallback(List<Category> oldList, List<Category> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }
        
        @Override
        public int getOldListSize() {
            return oldList.size();
        }
        
        @Override
        public int getNewListSize() {
            return newList.size();
        }
        
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return Objects.equals(
                oldList.get(oldItemPosition).getId(),
                newList.get(newItemPosition).getId()
            );
        }
        
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Category oldItem = oldList.get(oldItemPosition);
            Category newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.getName(), newItem.getName())
                && Double.compare(oldItem.getBalance(), newItem.getBalance()) == 0
                && oldItem.getBudget() == newItem.getBudget();
        }
    }
}
