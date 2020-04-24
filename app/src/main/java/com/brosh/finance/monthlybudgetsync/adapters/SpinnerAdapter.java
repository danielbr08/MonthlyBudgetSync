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
    private List<String> data;
    private Activity activity;
    private LayoutInflater inflater;

    public SpinnerAdapter(List<String> data, Activity activity) {
        this.data = data;
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if (convertView == null)
            view = inflater.inflate(R.layout.custom_spinner, null);
        TextView tv = (TextView) view;
        tv.setText(data.get(position));
        return view;
    }

//    @Override
//    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        View view = super.getDropDownView(position, convertView, parent);
//
//    }
}
