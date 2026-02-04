package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;

import java.util.List;

public class TransactionsViewAdapter extends RecyclerView.Adapter<TransactionViewHolder> {
    private final LayoutInflater mInflater;
    private final List<Transaction> transactions;
    private final boolean showCategory;

    public TransactionsViewAdapter(Context context, List<Transaction> transactions, boolean showCategory) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.transactions = transactions != null ? transactions : new java.util.ArrayList<>();
        this.showCategory = showCategory;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int transactionLayout = R.layout.transaction_row_item;
        View view = this.mInflater.inflate(transactionLayout, parent, false);
        return new TransactionViewHolder(view, this.showCategory);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        if (position >= 0 && position < transactions.size()) {
            holder.onBindViewHolder(transactions.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
