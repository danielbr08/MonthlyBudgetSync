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
    private LayoutInflater mInflater;
    private List<Transaction> transaction;
    private boolean showCategory;

    public TransactionsViewAdapter(Context context, List<Transaction> transaction, boolean showCategory) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.transaction = transaction;
        this.showCategory = showCategory;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        int transactionLayout = this.showCategory ? R.layout.transaction_row_item : R.layout.transaction_row_item_no_category;
        int transactionLayout = R.layout.transaction_row_item;
        View view = this.mInflater.inflate(transactionLayout, parent, false);
        return new TransactionViewHolder(view, this.showCategory);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.onBindViewHolder(transaction.get(position));
    }

    @Override
    public int getItemCount() {
        return transaction.size();
    }
}
