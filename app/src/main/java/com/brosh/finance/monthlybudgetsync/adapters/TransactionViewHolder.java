package com.brosh.finance.monthlybudgetsync.adapters;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.objects.Transaction;
import com.brosh.finance.monthlybudgetsync.utils.DateUtil;
import com.brosh.finance.monthlybudgetsync.utils.FormatUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.util.Arrays;
import java.util.List;

/**
 * ViewHolder for displaying transactions in a RecyclerView.
 * Handles formatting, styling, and strike-through for deleted transactions.
 */
public class TransactionViewHolder extends RecyclerView.ViewHolder {

    // ============================================
    // CONSTANTS
    // ============================================

    private static final int HEADER_TEXT_SIZE = 12;
    private static final String EMPTY = "";
    
    // Price colors
    private static final int COLOR_POSITIVE = 0xFF10B981; // Green
    private static final int COLOR_NEGATIVE = 0xFFEF4444; // Red
    
    // Payment method display mappings (for legacy data)
    private static final String CREDIT_CARD_LONG_HE = "כרטיס אשראי";
    private static final String CREDIT_CARD_SHORT_HE = "כ. אשראי";

    // ============================================
    // UI COMPONENTS
    // ============================================

    private final TextView idTV;
    private final TextView catNameTV;
    private final TextView paymentMethodTV;
    private final TextView storeTV;
    private final TextView chargeDateTV;
    private final TextView priceTV;
    private final List<TextView> allTextViews;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public TransactionViewHolder(@NonNull View itemView, boolean showCategory) {
        super(itemView);

        this.idTV = itemView.findViewById(R.id.trn_id);
        this.catNameTV = itemView.findViewById(R.id.trn_category);
        this.paymentMethodTV = itemView.findViewById(R.id.trn_payment_method);
        this.storeTV = itemView.findViewById(R.id.trn_store);
        this.chargeDateTV = itemView.findViewById(R.id.trn_charge_date);
        this.priceTV = itemView.findViewById(R.id.trn_price);
        this.allTextViews = Arrays.asList(idTV, catNameTV, paymentMethodTV, storeTV, chargeDateTV, priceTV);
    }

    // ============================================
    // DATA BINDING
    // ============================================

    /**
     * Binds transaction data to the ViewHolder.
     * Uses FormatUtil for consistent number formatting.
     *
     * @param transaction the transaction data to display
     */
    public void onBindViewHolder(@NonNull Transaction transaction) {
        boolean isTotalRow = transaction.getId() == null;
        
        if (isTotalRow) {
            bindTotalRow(transaction);
        } else {
            bindTransactionRow(transaction);
        }

        // Set price color based on value
        setPriceColor(transaction.getPrice());

        setStrikeThroughText(transaction.isDeleted());

        if (isTotalRow) {
            UiUtil.setHeaderProperties(allTextViews, HEADER_TEXT_SIZE, false);
        }
    }
    
    /**
     * Binds data for a regular transaction row.
     */
    private void bindTransactionRow(@NonNull Transaction transaction) {
        idTV.setText(String.valueOf(transaction.getIdPerMonth()));
        catNameTV.setText(transaction.getCategory());
        paymentMethodTV.setText(formatPaymentMethodForDisplay(transaction.getPaymentMethod()));
        storeTV.setText(transaction.getShop());
        chargeDateTV.setText(DateUtil.convertDateToString(transaction.getPayDate(), Config.DATE_FORMAT_SHORT));
        priceTV.setText(FormatUtil.formatDecimal(transaction.getPrice()));
    }
    
    /**
     * Formats payment method for display, converting legacy long text to short form.
     * @param paymentMethod the original payment method text
     * @return the formatted payment method text
     */
    private String formatPaymentMethodForDisplay(String paymentMethod) {
        if (paymentMethod == null) return EMPTY;
        // Convert old Hebrew credit card text to shortened form
        if (CREDIT_CARD_LONG_HE.equals(paymentMethod)) {
            return CREDIT_CARD_SHORT_HE;
        }
        return paymentMethod;
    }
    
    /**
     * Binds data for the total row (shows sum of transactions).
     */
    private void bindTotalRow(@NonNull Transaction transaction) {
        idTV.setText(transaction.getCategory()); // Total label
        catNameTV.setText(EMPTY);
        paymentMethodTV.setText(EMPTY);
        storeTV.setText(EMPTY);
        chargeDateTV.setText(EMPTY);
        priceTV.setText(FormatUtil.formatDecimal(transaction.getPrice()));
    }
    
    /**
     * Sets the price text color based on whether the value is positive or negative.
     * Green for positive/zero, Red for negative.
     *
     * @param price the price value
     */
    private void setPriceColor(double price) {
        if (price < 0) {
            priceTV.setTextColor(COLOR_NEGATIVE);
        } else {
            priceTV.setTextColor(COLOR_POSITIVE);
        }
    }

    // ============================================
    // STYLING
    // ============================================

    /**
     * Sets strike-through style on all text views.
     * Used for deleted transactions.
     *
     * @param enabled true to show strike-through, false to remove it
     */
    public void setStrikeThroughText(boolean enabled) {
        UiUtil.strikeThroughText(allTextViews, enabled);
    }
}
