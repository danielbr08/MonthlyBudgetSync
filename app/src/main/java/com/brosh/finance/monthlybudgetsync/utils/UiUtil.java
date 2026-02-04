package com.brosh.finance.monthlybudgetsync.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.adapters.SpinnerAdapter;
import com.brosh.finance.monthlybudgetsync.config.Config;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UiUtil {
    
    private UiUtil() {
        // Utility class
    }

    public static void setHeaderProperties(TextView tv, int textSize, boolean clickable) {
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(textSize);
        tv.setClickable(clickable);
        if (clickable)
            Linkify.addLinks(tv, Linkify.ALL);
    }

    public static void setHeaderProperties(List<TextView> textViews, int textSize, boolean clickable) {
        for (TextView tv : textViews) {
            setHeaderProperties(tv, textSize, clickable);
        }
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public static void reverseLinearLayout(LinearLayout linearLayout) {
        for (int i = linearLayout.getChildCount() - 1; i >= 0; i--) {
            View item = linearLayout.getChildAt(i);
            linearLayout.removeViewAt(i);
            linearLayout.addView(item);
        }
    }

    public static void strikeThroughText(TextView tv) {
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @SuppressWarnings("unused") // Used internally by strikeThroughText
    public static void unStrikeThroughText(TextView tv) {
        tv.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
    }

    public static void strikeThroughText(List<TextView> textViews, boolean enabled) {
        for (TextView tv : textViews) {
            if (enabled) {
                strikeThroughText(tv);
            } else {
                unStrikeThroughText(tv);
            }
        }
    }

    public static void setWidthCreateBudgetPageDataWidgets(List<View> widgets, int screenWidthReduceButtonSize, int wrapContent) {
        // Widgets are order by : categoryNameET, categoryValueET, constPaymentCB, shopET, optionalDaysSpinner
        int i = 0;
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CATEGORY_NAME_ET_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CATEGORY_VALUE_ET_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CONST_PAYMENT_CB_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.SHOP_ET_WIDTH_PERCENT), wrapContent));
        widgets.get(i).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.OPTIONAL_DAYS_SPINNER_WIDTH_PERCENT), wrapContent));
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public static void setWidthCreateBudgetPageTitleWidgets(List<View> widgets, int screenWidthReduceButtonSize, int wrapContent) {
        // Widgets are order by : categoryNameET, categoryValueET, constPaymentCB, shopET, payDateTV
        int i = 0;
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CATEGORY_NAME_TV_TITLE_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CATEGORY_VALUE_TV_TITLE_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CONST_PAYMENT_TV_TITLE_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.SHOP_TV_TITLE_WIDTH_PERCENT), wrapContent));
        widgets.get(i).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.PAY_DATE_TITLE_WIDTH_PERCENT), wrapContent));
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public static void setTextTitleWidgets(List<View> widgets, List<String> titlesNames) {
        // Widgets are order by : categoryNameET, categoryValueET, constPaymentCB, shopET, payDateTV
        int i = 0;
        ((TextView) widgets.get(i)).setText(TextUtil.getWordCapitalLetter(titlesNames.get(i++)));
        ((TextView) widgets.get(i)).setText(TextUtil.getWordCapitalLetter(titlesNames.get(i++)));
        ((TextView) widgets.get(i)).setText(TextUtil.getWordCapitalLetter(titlesNames.get(i++)));
        ((TextView) widgets.get(i)).setText(TextUtil.getWordCapitalLetter(titlesNames.get(i++)));
        ((TextView) widgets.get(i)).setText(TextUtil.getWordCapitalLetter(titlesNames.get(i)));
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public static void setTitleStyle(List<TextView> titlesTV) {
        for (TextView titletv : titlesTV) {
            setHeaderProperties(titletv, 15, true);
        }
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public static void setRowStrikeThroughStyle(List<TextView> tvRow) {
        for (TextView tv : tvRow) {
            strikeThroughText(tv);
        }
    }

    public static void setTxtSize(View view, float size) {
        if (view instanceof TextView textView)
            textView.setTextSize(size);
        else if (view instanceof EditText editText)
            editText.setTextSize(size);
        else if (view instanceof CheckBox checkBox)
            checkBox.setTextSize(size);
    }

    public static void setTxtSize(List<View> views, float size) {
        for (View view : views) {
            setTxtSize(view, size);
        }
    }

    public static void setViewInputTypeText(View view) {
        int inputTypeText = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
        if (view instanceof EditText editText)
            editText.setInputType(inputTypeText);
        else if (view instanceof CheckBox checkBox)
            checkBox.setInputType(inputTypeText);
    }

    public static void setViewInputTypeNumber(View view) {
        int inputTypeNumber = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL;
        if (view instanceof EditText editText)
            editText.setInputType(inputTypeNumber);
    }

    public static void setViewsText(List<View> views, List<String> viewsText) {
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            if (view instanceof EditText editText) {
                String val = viewsText.get(i);
                editText.setText(val);
            } else if (view instanceof Spinner spinner) {
                int val = Integer.parseInt(viewsText.get(i));
                spinner.setSelection(val);
            } else if (view instanceof CheckBox checkBox) {
                boolean val = Boolean.parseBoolean(viewsText.get(i));
                checkBox.setChecked(val);
            }
        }
    }

    public static void setInputFocus(View view) {
        view.requestFocus();
    }

    public static void setTotalBudgetRow(List<TextView> totalBudgerRow) {
        int i = 0;
        TextView category = totalBudgerRow.get(i++);
        TextView budget = totalBudgerRow.get(i++);
        TextView balance = totalBudgerRow.get(i);
        category.setTypeface(null, Typeface.BOLD);
        category.setTextSize(13);
        budget.setTypeface(null, Typeface.BOLD);
        budget.setTextSize(13);
        balance.setTypeface(null, Typeface.BOLD);
        balance.setTextSize(13);
        setTextViewColor(totalBudgerRow, Color.BLACK);
    }

    public static void setTextViewColor(List<TextView> textViews, int color) {
        for (TextView tv : textViews) {
            tv.setTextColor(color);
        }
    }

    public static void setDaysInMonthSpinner(Spinner spinner, Activity activity) {
        List<Integer> daysInMonth = IntStream.range(1, 31).boxed().collect(Collectors.toList());
        List<String> daysInMonthStringList = Lists.transform(daysInMonth, Functions.toStringFunction());
        SpinnerAdapter adapter = new SpinnerAdapter(daysInMonthStringList, activity, R.layout.custom_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(1, true);
        spinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    public static void addAdvertiseToActivity(Context context) {
        MobileAds.initialize(context, initializationStatus -> { });
        AdView adView = ((Activity) context).findViewById(R.id.adView);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    public static List<View> findAllTextviews(ViewGroup viewGroup) {
        List<View> textViews = new ArrayList<>();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup vg) {
                List<View> temp = findAllTextviews(vg);
                if (temp != null)
                    textViews.addAll(temp);
            } else if (view instanceof TextView textView) {
                textViews.add(textView);
            }
        }
        return textViews;
    }

    public static void restoreBackground(List<View> views, Drawable drawable) {
        for (View v : views) {
            v.setBackground(drawable);
        }
    }

    public static List<Integer> getIdTVByName(ViewGroup viewGroup, String keyName) {
        List<Integer> textViewID = new ArrayList<>();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup vg) {
                List<Integer> temp = getIdTVByName(vg, keyName);
                if (temp != null)
                    textViewID.addAll(temp);
            } else if (view instanceof TextView textView) {
                if (textView.getText().toString().equals(keyName)) {
                    textViewID.add(view.getId());
                }
            }
        }
        return textViewID;
    }

    public static void setToolbar(AppCompatActivity appCompatActivity, String yearMonth) {
        ActionBar actionBar = appCompatActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.action_bar_layout);
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(appCompatActivity, R.color.colorApp)));
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        setTitleText(appCompatActivity, yearMonth);
    }

    @SuppressWarnings("unused") // Used internally by setToolbar
    public static void setTitleText(AppCompatActivity appCompatActivity, String yearMonth) {
        String title = appCompatActivity.getString(R.string.app_name);
        title += yearMonth != null ? "\n" + yearMonth : "";
        TextView tvTitle = appCompatActivity.findViewById(R.id.tv_title);
        tvTitle.setText(title);
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public static void openShareDialog(AlertDialog.Builder builder, AlertDialog dialog, Context context, String hint, String title, int inputType,final EditText editTextInput, String positiveText, String negativeText, View.OnClickListener onClickListener) {
        editTextInput.setHint(hint);
        editTextInput.setInputType(inputType);
        builder.setTitle(title);

        builder.setView(editTextInput);
        builder.setPositiveButton(positiveText, null);
        builder.setNegativeButton(negativeText, (d, which) -> d.cancel());
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(onClickListener);
    }
}