package com.brosh.finance.monthlybudgetsync.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.services.UIService;

import java.text.DecimalFormat;

public class CreateBudgetViewHolder extends RecyclerView.ViewHolder {

    private EditText catName;
    private EditText budget;
    private CheckBox constDate;
    private EditText store;
    private Spinner chargeDay;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CreateBudgetViewHolder(@NonNull View itemView, Context context) {
        super(itemView);

        this.catName = itemView.findViewById(R.id.bgt_category);
        this.budget = itemView.findViewById(R.id.bgt_budget);
        this.constDate = itemView.findViewById(R.id.bgt_constant_date);
        this.store = itemView.findViewById(R.id.bgt_store);
        this.chargeDay = itemView.findViewById(R.id.bgt_charge_day);

        final EditText store = this.store;
        final Spinner chargeDay = this.chargeDay;

        this.constDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int visibilty = isChecked ? View.VISIBLE : View.INVISIBLE;
                store.setVisibility(visibilty);
                chargeDay.setVisibility(visibilty);
            }
        });

        UIService.setDaysInMonthSpinner(this.chargeDay, (Activity) context);
    }

    public void onBindViewHolder(Budget budget, int position) {
        DecimalFormat decim = new DecimalFormat("#,###.##");

        int visibilty = budget.isConstPayment() ? View.VISIBLE : View.INVISIBLE;
        String store = budget.getShop() != null ? budget.getShop() : "";
        this.catName.setText(budget.getCategoryName());
        this.budget.setText(String.valueOf(decim.format(budget.getValue())));
        this.constDate.setChecked(budget.isConstPayment());
        this.store.setText(store);
        this.chargeDay.setSelection(budget.getChargeDay() - 1);
        this.catName.requestFocus();

//        if(position%2 != 0)
//            itemView.setBackgroundColor(Color.LTGRAY);
//        else
//            itemView.setBackgroundColor(Color.DKGRAY);

        this.store.setVisibility(visibilty);
        this.chargeDay.setVisibility(visibilty);
    }
}
