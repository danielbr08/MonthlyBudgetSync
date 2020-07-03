package com.brosh.finance.monthlybudgetsync.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.objects.Budget;
import com.brosh.finance.monthlybudgetsync.utils.UiUtil;

import java.text.DecimalFormat;
import java.util.List;

public class CreateBudgetViewHolder extends RecyclerView.ViewHolder {

    private EditText catName;
    private EditText budget;
    private CheckBox constDate;
    private EditText store;
    private TextView chargeDay;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CreateBudgetViewHolder(@NonNull View itemView, Context context) {
        super(itemView);

        catName = itemView.findViewById(R.id.bgt_category);
        budget = itemView.findViewById(R.id.bgt_budget);
        constDate = itemView.findViewById(R.id.bgt_constant_date);
        store = itemView.findViewById(R.id.bgt_store);
        chargeDay = itemView.findViewById(R.id.bgt_charge_day);

        View.OnLongClickListener eventLongClick = new View.OnLongClickListener() { // todo take it outside to external file( eventListener file)
            @Override
            public boolean onLongClick(View v) {
                View parent = (View) v.getParent();
                parent.performLongClick();
                return false;
            }
        };

        chargeDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = ((Activity) context).getLayoutInflater().inflate(R.layout.day_peeker, null);
                builder.setTitle(R.string.select_charge_day);
                builder.setView(view);
                AlertDialog alertDialog = builder.create();
                List<View> textViews = UiUtil.findAllTextviews((ViewGroup) view);
                for (View tv : textViews) {
                    tv.setOnClickListener(tv1 -> {
                        String daySelected1 = ((TextView) tv1).getText().toString();
                        ((TextView) v).setText(daySelected1);
                        UiUtil.restoreBackground(textViews, v.getBackground());
                        tv1.setBackgroundResource(R.drawable.circle_style);
                        alertDialog.dismiss();
                    });
                }
                alertDialog.show();
            }
        });

        catName.setOnLongClickListener(eventLongClick);
        budget.setOnLongClickListener(eventLongClick);
        store.setOnLongClickListener(eventLongClick);
//        constDate.setOnLongClickListener(eventLongClick);
//        chargeDay.setOnLongClickListener(eventLongClick);

        final EditText store = this.store;
        final TextView chargeDay = this.chargeDay;

        this.constDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int visibilty = isChecked ? View.VISIBLE : View.INVISIBLE;
                store.setVisibility(visibilty);
                chargeDay.setVisibility(visibilty);
            }
        });
//        UiUtil.setDaysInMonthSpinner(this.chargeDay, (Activity) context);
    }

    public void onBindViewHolder(Budget budget, int position) {
        DecimalFormat decim = new DecimalFormat("#,###.##");

        int visibilty = budget.isConstPayment() ? View.VISIBLE : View.INVISIBLE;
        String store = budget.getShop() != null ? budget.getShop() : "";
        this.catName.setText(budget.getCategoryName());
        this.budget.setText(decim.format(budget.getValue()));
        this.constDate.setChecked(budget.isConstPayment());
        this.store.setText(store);
        this.chargeDay.setText(String.valueOf(budget.getChargeDay()));

        this.catName.requestFocus();

//        if(position%2 != 0)
//            itemView.setBackgroundColor(Color.LTGRAY);
//        else
//            itemView.setBackgroundColor(Color.DKGRAY);

        this.store.setVisibility(visibilty);
        this.chargeDay.setVisibility(visibilty);
    }
}
