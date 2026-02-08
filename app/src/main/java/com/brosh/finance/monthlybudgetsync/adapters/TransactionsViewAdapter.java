package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RecyclerView adapter for displaying transactions.
 * Uses DiffUtil for efficient updates.
 */
public class TransactionsViewAdapter extends RecyclerView.Adapter<TransactionViewHolder> {

    // ============================================
    // INTERFACES
    // ============================================

    /**
     * Callback interface for transaction long-click events.
     */
    public interface OnTransactionLongClickListener {
        /**
         * Called when a transaction row is long-clicked.
         * @param transaction the transaction that was long-clicked
         */
        void onTransactionLongClick(@NonNull Transaction transaction);
    }

    // ============================================
    // FIELDS
    // ============================================

    private final LayoutInflater mInflater;
    private final List<Transaction> transactions;
    private final boolean showCategory;
    @Nullable
    private OnTransactionLongClickListener longClickListener;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public TransactionsViewAdapter(Context context, List<Transaction> transactions, boolean showCategory) {
        this.mInflater = LayoutInflater.from(context);
        this.transactions = transactions != null ? new ArrayList<>(transactions) : new ArrayList<>();
        this.showCategory = showCategory;
        setHasStableIds(true);
    }

    /**
     * Sets the long-click listener for transaction rows.
     * @param listener the listener to set
     */
    public void setOnTransactionLongClickListener(@Nullable OnTransactionLongClickListener listener) {
        this.longClickListener = listener;
    }

    // ============================================
    // ADAPTER METHODS
    // ============================================

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.transaction_row_item, parent, false);
        return new TransactionViewHolder(view, showCategory);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        if (position >= 0 && position < transactions.size()) {
            Transaction transaction = transactions.get(position);
            holder.onBindViewHolder(transaction);
            
            // Set long-click listener for non-total rows
            if (transaction.getId() != null && longClickListener != null) {
                holder.itemView.setOnLongClickListener(v -> {
                    longClickListener.onTransactionLongClick(transaction);
                    return true;
                });
            } else {
                holder.itemView.setOnLongClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
    
    @Override
    public long getItemId(int position) {
        Transaction transaction = transactions.get(position);
        return transaction.getId() != null ? transaction.getId().hashCode() : position;
    }

    // ============================================
    // DATA OPERATIONS
    // ============================================

    /**
     * Updates the adapter with new data using DiffUtil for efficient updates.
     *
     * @param newTransactions the new list of transactions
     */
    public void updateData(@NonNull List<Transaction> newTransactions) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
            new TransactionDiffCallback(transactions, newTransactions)
        );
        transactions.clear();
        transactions.addAll(newTransactions);
        diffResult.dispatchUpdatesTo(this);
    }

    // ============================================
    // INNER CLASSES
    // ============================================

    /**
     * DiffUtil callback for efficient RecyclerView updates.
     */
    private static class TransactionDiffCallback extends DiffUtil.Callback {
        private final List<Transaction> oldList;
        private final List<Transaction> newList;
        
        TransactionDiffCallback(List<Transaction> oldList, List<Transaction> newList) {
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
            Transaction oldItem = oldList.get(oldItemPosition);
            Transaction newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.getId(), newItem.getId())
                && Objects.equals(oldItem.getCategory(), newItem.getCategory())
                && Double.compare(oldItem.getPrice(), newItem.getPrice()) == 0
                && oldItem.isDeleted() == newItem.isDeleted();
        }
    }
}
