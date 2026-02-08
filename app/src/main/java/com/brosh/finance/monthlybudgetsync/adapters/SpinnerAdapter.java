package com.brosh.finance.monthlybudgetsync.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.brosh.finance.monthlybudgetsync.R;

import java.util.List;

public class SpinnerAdapter extends BaseAdapter {
    private final List<String> data;
    private final LayoutInflater inflater;
    private final int spinnerType;
    private final int dropdownType;

    public SpinnerAdapter(List<String> data, Activity activity, int spinnerType) {
        this(data, activity, spinnerType, R.layout.spinner_dropdown_item);
    }

    public SpinnerAdapter(List<String> data, Activity activity, int spinnerType, int dropdownType) {
        this.data = data;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.spinnerType = spinnerType;
        this.dropdownType = dropdownType;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(spinnerType, parent, false);
        }
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setText(data.get(position));
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(dropdownType, parent, false);
        }
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setText(data.get(position));
        }
        return view;
    }
}
