package com.brosh.finance.monthlybudgetsync.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.utils.FormatUtil;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.util.Arrays;
import java.util.List;

/**
 * ViewHolder for budget creation items in RecyclerView.
 * Handles budget row display, day picker dialog, and constant payment visibility.
 */
public class CreateBudgetViewHolder extends RecyclerView.ViewHolder {

    // ============================================
    // VIEW REFERENCES
    // ============================================
    
    private final EditText catName;
    private final EditText budget;
    private final CheckBox constDate;
    private final EditText store;
    private final TextView chargeDay;

    // ============================================
    // DAY PICKER STATE
    // ============================================
    
    @Nullable
    private View dayPickerView;
    @Nullable
    private TextView selectedDayTV;
    @Nullable
    private TextView previousSelectedDayTV;
    @Nullable
    private TextView defaultSelectionTV;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public CreateBudgetViewHolder(@NonNull View itemView) {
        super(itemView);

        catName = itemView.findViewById(R.id.bgt_category);
        budget = itemView.findViewById(R.id.bgt_budget);
        constDate = itemView.findViewById(R.id.bgt_constant_date);
        store = itemView.findViewById(R.id.bgt_store);
        chargeDay = itemView.findViewById(R.id.bgt_charge_day);

        setupConstDateListener();
    }

    // ============================================
    // SETUP METHODS
    // ============================================

    /**
     * Sets up the constant payment checkbox listener.
     * Shows/hides store and charge day fields based on checkbox state.
     */
    private void setupConstDateListener() {
        constDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.INVISIBLE;
            store.setVisibility(visibility);
            chargeDay.setVisibility(visibility);
        });
    }

    // ============================================
    // BINDING
    // ============================================

    /**
     * Binds budget data to the ViewHolder.
     * 
     * @param budgetData the budget to display
     * @param position adapter position
     * @param context the activity context
     */
    public void onBindViewHolder(@NonNull Budget budgetData, int position, @NonNull Context context) {
        bindBudgetData(budgetData);
        setupDayPicker(context);
        setupChargeDayClickListener(context);
    }

    /**
     * Binds the budget data to the views.
     */
    private void bindBudgetData(@NonNull Budget budgetData) {
        int visibility = budgetData.isConstPayment() ? View.VISIBLE : View.INVISIBLE;
        String shopText = budgetData.getShop() != null ? budgetData.getShop() : "";

        catName.setText(budgetData.getCategoryName());
        budget.setText(FormatUtil.formatInteger(budgetData.getValue()));
        constDate.setChecked(budgetData.isConstPayment());
        store.setText(shopText);
        chargeDay.setText(String.valueOf(budgetData.getChargeDay()));

        catName.requestFocus();

        store.setVisibility(visibility);
        chargeDay.setVisibility(visibility);
    }

    // ============================================
    // DAY PICKER DIALOG
    // ============================================

    /**
     * Initializes the day picker view and sets up the default selection.
     */
    private void setupDayPicker(@NonNull Context context) {
        dayPickerView = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.day_peeker, null);
        
        initializeDefaultSelection();
    }

    /**
     * Initializes the default selected day in the picker.
     */
    private void initializeDefaultSelection() {
        if (dayPickerView == null) return;

        String chargeDayText = chargeDay.getText().toString();
        List<Integer> ids = UiUtil.getIdTVByName((ViewGroup) dayPickerView, chargeDayText);
        int defaultId = (ids != null && !ids.isEmpty()) ? ids.get(0) : R.id.tv1;

        defaultSelectionTV = dayPickerView.findViewById(defaultId);
        if (defaultSelectionTV != null) {
            defaultSelectionTV.setBackgroundResource(R.drawable.circle_pink_style);
        }
        
        selectedDayTV = defaultSelectionTV;
        previousSelectedDayTV = defaultSelectionTV;
    }

    /**
     * Sets up the charge day field click listener to show the day picker dialog.
     */
    private void setupChargeDayClickListener(@NonNull Context context) {
        chargeDay.setOnClickListener(v -> showDayPickerDialog(context, v));
    }

    /**
     * Shows the day picker dialog.
     */
    private void showDayPickerDialog(@NonNull Context context, @NonNull View clickedView) {
        if (dayPickerView == null) return;

        // Remove from parent if already attached
        removeViewFromParent(dayPickerView);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.select_charge_day);
        builder.setView(dayPickerView);
        builder.setPositiveButton(R.string.select, (dialog, which) -> onDaySelected());
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> onDaySelectionCancelled(clickedView));

        AlertDialog alertDialog = builder.create();
        setupDayPickerClickListeners(clickedView);
        alertDialog.show();
    }

    /**
     * Removes a view from its parent if it has one.
     */
    private void removeViewFromParent(@NonNull View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    /**
     * Sets up click listeners for all day options in the picker.
     */
    private void setupDayPickerClickListeners(@NonNull View clickedView) {
        if (dayPickerView == null) return;

        List<View> textViews = UiUtil.findAllTextviews((ViewGroup) dayPickerView);
        for (View tv : textViews) {
            tv.setOnClickListener(dayView -> onDayOptionClicked(dayView, clickedView));
        }
    }

    /**
     * Handles click on a day option in the picker.
     */
    private void onDayOptionClicked(@NonNull View dayView, @NonNull View clickedView) {
        previousSelectedDayTV = selectedDayTV;
        selectedDayTV = (TextView) dayView;

        // Reset previous selection
        if (previousSelectedDayTV != null) {
            UiUtil.restoreBackground(Arrays.asList(previousSelectedDayTV), clickedView.getBackground());
        }

        // Highlight new selection
        if (selectedDayTV != null) {
            selectedDayTV.setBackgroundResource(R.drawable.circle_pink_style);
        }
    }

    /**
     * Handles positive button click - applies the selected day.
     */
    private void onDaySelected() {
        if (selectedDayTV != null) {
            defaultSelectionTV = selectedDayTV;
            chargeDay.setText(selectedDayTV.getText().toString());
        }
    }

    /**
     * Handles negative button click - restores previous selection.
     */
    private void onDaySelectionCancelled(@NonNull View clickedView) {
        if (selectedDayTV != null && dayPickerView != null) {
            UiUtil.restoreBackground(Arrays.asList(selectedDayTV), dayPickerView.getBackground());
        }

        if (defaultSelectionTV != null) {
            defaultSelectionTV.setBackgroundResource(R.drawable.circle_pink_style);
        }

        previousSelectedDayTV = defaultSelectionTV;
        selectedDayTV = defaultSelectionTV;
    }
}
