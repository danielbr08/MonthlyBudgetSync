package com.brosh.finance.monthlybudgetsync.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.services.DateService;
import com.brosh.finance.monthlybudgetsync.services.UIService;

import java.text.DecimalFormat;
import java.util.Arrays;

public class TransactionViewHolder extends RecyclerView.ViewHolder {
    private boolean showCategory;
    private TextView id;
    private TextView catName;
    private TextView paymentMethod;
    private TextView store;
    private TextView chargeDate;
    private TextView price;

    public TransactionViewHolder(@NonNull View itemView, boolean showCategory) {
        super(itemView);

        this.id = itemView.findViewById(R.id.trn_id);
//        if (showCategory)
        this.catName = itemView.findViewById(R.id.trn_category);
        this.paymentMethod = itemView.findViewById(R.id.trn_payment_method);
        this.store = itemView.findViewById(R.id.trn_store);
        this.chargeDate = itemView.findViewById(R.id.trn_charge_date);
        this.price = itemView.findViewById(R.id.trn_price);
        this.showCategory = showCategory;
    }

    public void onBindViewHolder(Transaction transaction) {
        DecimalFormat decim = new DecimalFormat("#,###.##");

        if (transaction.getId() == null) { // Total(last) row
            this.id.setText(transaction.getCategory());// Get Total label
//            if (showCategory)
            this.catName.setText("");
            this.paymentMethod.setText("");
            this.store.setText("");
            this.chargeDate.setText("");
            this.price.setText(decim.format(transaction.getPrice()));
        } else {
            this.id.setText(String.valueOf(transaction.getIdPerMonth()));
//            if (showCategory)
            this.catName.setText(transaction.getCategory());
            this.paymentMethod.setText(transaction.getPaymentMethod());
            this.store.setText(transaction.getShop());
            this.chargeDate.setText(DateService.convertDateToString(transaction.getPayDate(), Config.DATE_FORMAT));
            this.price.setText(decim.format(transaction.getPrice()));
        }

        if (transaction.getIsStorno())
//            if (showCategory)
//                UIService.strikeThroughText(Arrays.asList(this.id, this.catName, this.paymentMethod, this.store, this.chargeDate, this.price));
//            else
//            UIService.strikeThroughText(Arrays.asList(this.id, this.paymentMethod, this.store, this.chargeDate, this.price));
            UIService.strikeThroughText(Arrays.asList(this.id, this.catName, this.paymentMethod, this.store, this.chargeDate, this.price));

        if (transaction.getId() == null) {
//           if (showCategory)
//                UIService.strikeThroughText(Arrays.asList(this.id, this.paymentMethod, this.store, this.chargeDate, this.price));
//            else
            UIService.setHeaderProperties(Arrays.asList(this.id, this.catName, this.paymentMethod, this.store, this.chargeDate, this.price), 12, false);
        }
    }
}
