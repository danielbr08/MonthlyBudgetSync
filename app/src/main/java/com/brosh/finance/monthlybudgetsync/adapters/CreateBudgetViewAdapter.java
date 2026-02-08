package com.brosh.finance.monthlybudgetsync.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Budget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RecyclerView adapter for creating and editing budget items.
 * 
 * Interaction patterns:
 * - Long press: Select row and start drag to reorder
 * - Double tap: Duplicate row
 * - Swipe: Delete row
 */
public class CreateBudgetViewAdapter extends RecyclerView.Adapter<CreateBudgetViewHolder> {

    // Timing constants
    private static final long DOUBLE_TAP_TIMEOUT = 300; // ms between taps for double-tap
    
    // Colors for selection state
    private static final int COLOR_SELECTED = 0x4432E0C4; // Semi-transparent accent
    
    private final LayoutInflater mInflater;
    private final List<Budget> budgets;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    @Nullable
    private ItemTouchHelper itemTouchHelper;
    
    // Track selected/active row
    private int selectedPosition = RecyclerView.NO_POSITION;
    
    // Double-tap detection
    private long lastTapTime = 0;
    private int lastTapPosition = RecyclerView.NO_POSITION;

    public CreateBudgetViewAdapter(Context context, List<Budget> budgets) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.budgets = budgets != null ? new ArrayList<>(budgets) : new ArrayList<>();
        this.context = context;
    }
    
    /**
     * Sets the ItemTouchHelper for drag functionality.
     */
    public void setItemTouchHelper(@Nullable ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }
    
    /**
     * Clears the current selection.
     */
    public void clearSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION;
        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition);
        }
    }
    
    /**
     * Updates the adapter data using DiffUtil for efficient updates.
     */
    public void updateData(List<Budget> newBudgets) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new BudgetDiffCallback(this.budgets, newBudgets));
        this.budgets.clear();
        this.budgets.addAll(newBudgets);
        diffResult.dispatchUpdatesTo(this);
    }
    
    /**
     * Adds a new budget item and notifies the adapter.
     */
    public void addBudget(Budget budget) {
        this.budgets.add(budget);
        notifyItemInserted(this.budgets.size() - 1);
    }
    
    /**
     * Removes a budget item at the specified position.
     */
    public boolean removeBudget(int position) {
        if (position >= 0 && position < this.budgets.size() && this.budgets.size() > 1) {
            this.budgets.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, this.budgets.size() - position);
            clearSelection();
            return true;
        }
        return false;
    }
    
    /**
     * Duplicates a budget item at the specified position.
     */
    public boolean duplicateBudget(int position) {
        if (position >= 0 && position < this.budgets.size()) {
            Budget original = this.budgets.get(position);
            Budget duplicate = new Budget(
                original.getCategoryName(),
                original.getValue(),
                original.isConstPayment(),
                original.getShop(),
                original.getChargeDay(),
                this.budgets.size() + 1
            );
            this.budgets.add(position + 1, duplicate);
            notifyItemInserted(position + 1);
            return true;
        }
        return false;
    }
    
    /**
     * Moves a budget item from one position to another.
     */
    public boolean moveBudget(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= this.budgets.size() ||
            toPosition < 0 || toPosition >= this.budgets.size()) {
            return false;
        }
        
        Budget budget = this.budgets.remove(fromPosition);
        this.budgets.add(toPosition, budget);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
    
    /**
     * Returns the current list of budgets.
     */
    public List<Budget> getBudgets() {
        return budgets;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public CreateBudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = this.mInflater.inflate(R.layout.create_budget_row, parent, false);
        CreateBudgetViewHolder holder = new CreateBudgetViewHolder(view);
        
        // Long click listener for drag
        holder.itemView.setOnLongClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return false;
            
            // Clear previous selection
            clearSelection();
            
            // Select this row
            selectedPosition = position;
            holder.itemView.setBackgroundColor(COLOR_SELECTED);
            holder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            
            // Start drag
            if (itemTouchHelper != null) {
                itemTouchHelper.startDrag(holder);
            }
            
            return true;
        });
        
        // Click listener for double-tap detection
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
            
            long currentTime = System.currentTimeMillis();
            
            // Check for double tap
            if (position == lastTapPosition && 
                (currentTime - lastTapTime) < DOUBLE_TAP_TIMEOUT) {
                // Double tap detected - duplicate row
                duplicateBudget(position);
                holder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                
                // Reset
                lastTapTime = 0;
                lastTapPosition = RecyclerView.NO_POSITION;
            } else {
                // First tap - record it
                lastTapTime = currentTime;
                lastTapPosition = position;
                
                // Clear selection if tapping on a different row
                if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition != position) {
                    clearSelection();
                }
            }
        });
        
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CreateBudgetViewHolder holder, int position) {
        holder.onBindViewHolder(budgets.get(position), position, context);
        
        // Reset background based on selection state
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(COLOR_SELECTED);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.row_item_background);
        }
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }
    
    /**
     * DiffUtil callback for efficient budget list updates.
     */
    private static class BudgetDiffCallback extends DiffUtil.Callback {
        private final List<Budget> oldList;
        private final List<Budget> newList;

        BudgetDiffCallback(List<Budget> oldList, List<Budget> newList) {
            this.oldList = oldList != null ? oldList : new ArrayList<>();
            this.newList = newList != null ? newList : new ArrayList<>();
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
            Budget oldBudget = oldList.get(oldItemPosition);
            Budget newBudget = newList.get(newItemPosition);
            
            if (oldBudget.getId() != null && newBudget.getId() != null) {
                return Objects.equals(oldBudget.getId(), newBudget.getId());
            }
            return Objects.equals(oldBudget.getCategoryName(), newBudget.getCategoryName())
                    && oldBudget.getCatPriority() == newBudget.getCatPriority();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return Objects.equals(oldList.get(oldItemPosition), newList.get(newItemPosition));
        }
    }
}
