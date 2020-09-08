package com.brosh.finance.monthlybudgetsync.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Arrays;
import java.util.List;

public class CreateBudgetViewHolder extends RecyclerView.ViewHolder {

    private EditText catName;
    private EditText budget;
    private CheckBox constDate;
    private EditText store;
    private TextView chargeDay;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CreateBudgetViewHolder(@NonNull View itemView) {
        super(itemView);

        catName = itemView.findViewById(R.id.bgt_category);
        budget = itemView.findViewById(R.id.bgt_budget);
        constDate = itemView.findViewById(R.id.bgt_constant_date);
        store = itemView.findViewById(R.id.bgt_store);
        chargeDay = itemView.findViewById(R.id.bgt_charge_day);

        // todo take it outside to external file( eventListener file)
        View.OnLongClickListener eventLongClick = v -> {
            View parent = (View) v.getParent();
            parent.performLongClick();
            return false;
        };

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
    }

    public void onBindViewHolder(Budget budget, int position, Context context) {
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


        View dayPeekerView = ((Activity) context).getLayoutInflater().inflate(R.layout.day_peeker, null);
        Integer defaultId = chargeDay.getText().toString() == null ? R.id.tv1 : UiUtil.getIdTVByName((ViewGroup) dayPeekerView, chargeDay.getText().toString()).get(0);
        final TextView defaultSelectionTV[] = {dayPeekerView.findViewById(defaultId)};
        defaultSelectionTV[0].setBackgroundResource(R.drawable.circle_pink_style);
        final TextView selectedDay[] = {defaultSelectionTV[0]};
        final TextView prevSelectedDay[] = {defaultSelectionTV[0]};

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    defaultSelectionTV[0] = selectedDay[0];
                    String selectedDaytext = selectedDay[0].getText().toString();
                    chargeDay.setText(selectedDaytext);
                    dialog.dismiss();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked - rollback
                    UiUtil.restoreBackground(Arrays.asList(selectedDay[0]), dayPeekerView.getBackground());
                    defaultSelectionTV[0].setBackgroundResource(R.drawable.circle_pink_style);
                    prevSelectedDay[0] = defaultSelectionTV[0];
                    selectedDay[0] = defaultSelectionTV[0];

            }
        };

        chargeDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.select_charge_day);
                if (dayPeekerView.getParent() != null) {
                    ((ViewGroup) dayPeekerView.getParent()).removeView(dayPeekerView);
                }
                builder.setView(dayPeekerView).setPositiveButton(R.string.select, dialogClickListener)
                        .setNegativeButton(R.string.cancel, dialogClickListener);
                AlertDialog alertDialog = builder.create();
                List<View> textViews = UiUtil.findAllTextviews((ViewGroup) dayPeekerView);
                for (View tv : textViews) {
                    tv.setOnClickListener(tv1 -> {
                        prevSelectedDay[0] = selectedDay[0];
                        selectedDay[0] = (TextView) tv1;
                        UiUtil.restoreBackground(Arrays.asList(prevSelectedDay[0]), v.getBackground());
                        selectedDay[0].setBackgroundResource(R.drawable.circle_pink_style);
                    });
                }
                alertDialog.show();
            }
        });
    }
}
