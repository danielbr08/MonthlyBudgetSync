package com.brosh.finance.monthlybudgetsync.services;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.*;
import android.os.Build;
import android.text.InputType;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.RequiresApi;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;

import java.util.List;

public final class UIService {

    public static void setHeaderProperties(TextView tv) {
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(15);
        tv.setClickable(true);
        Linkify.addLinks(tv, Linkify.ALL);
    }

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

    public static void setWidthCreateBudgetPageDataWidgets(List<View> widgets, int screenWidthReduceButtonSize, int wrapContent) {
        // Widgets are order by : categoryNameET, categoryValueET, constPaymentCB, shopET, optionalDaysSpinner
        int i = 0;
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CATEGORY_NAME_ET_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CATEGORY_VALUE_ET_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CONST_PAYMENT_CB_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.SHOP_ET_WIDTH_PERCENT), wrapContent));
        widgets.get(i).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.OPTIONAL_DAYS_SPINNER_WIDTH_PERCENT), wrapContent));
    }

    public static void setWidthCreateBudgetPageTitleWidgets(List<View> widgets, int screenWidthReduceButtonSize, int wrapContent) {
        // Widgets are order by : categoryNameET, categoryValueET, constPaymentCB, shopET, payDateTV
        int i = 0;
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CATEGORY_NAME_TV_TITLE_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CATEGORY_VALUE_TV_TITLE_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.CONST_PAYMENT_TV_TITLE_WIDTH_PERCENT), wrapContent));
        widgets.get(i++).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.SHOP_TV_TITLE_WIDTH_PERCENT), wrapContent));
        widgets.get(i).setLayoutParams(new LinearLayout.LayoutParams((int) Math.floor(screenWidthReduceButtonSize * Config.PAY_DATE_TITLE_WIDTH_PERCENT), wrapContent));
    }

    public static void setTextTitleWidgets(List<View> widgets, List<String> titlesNames) {
        // Widgets are order by : categoryNameET, categoryValueET, constPaymentCB, shopET, payDateTV
        int i = 0;
        ((TextView) widgets.get(i)).setText(TextService.getWordCapitalLetter(titlesNames.get(i++)));
        ((TextView) widgets.get(i)).setText(TextService.getWordCapitalLetter(titlesNames.get(i++)));
        ((TextView) widgets.get(i)).setText(TextService.getWordCapitalLetter(titlesNames.get(i++)));
        ((TextView) widgets.get(i)).setText(TextService.getWordCapitalLetter(titlesNames.get(i++)));
        ((TextView) widgets.get(i)).setText(TextService.getWordCapitalLetter(titlesNames.get(i)));
    }

    public static void setTitleStyle(List<TextView> titlesTV) {//}, LinearLayout titleLL) {
        for (TextView titletv : titlesTV) {
            setHeaderProperties(titletv);
//            titleLL.addView(titletv);
        }
    }

    public static void setRowStrikeThroughStyle(List<TextView> tvRow) {
        for (TextView tv : tvRow) {
            strikeThroughText(tv);
        }
    }

    public static void setTxtSize(View view, float size) {
        if (view instanceof TextView)
            ((TextView) view).setTextSize(size);
        else if (view instanceof EditText)
            ((EditText) view).setTextSize(size);
        else if (view instanceof CheckBox)
            ((CheckBox) view).setTextSize(size);
    }

    public static void setTxtSize(List<View> views, float size) {
        for (View view : views) {
            setTxtSize(view, size);
        }
    }

    public static void setViewInputTypeText(View view) {
        int inputTypeText = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
        if (view instanceof EditText)
            ((EditText) view).setInputType(inputTypeText);
        else if (view instanceof CheckBox)
            ((CheckBox) view).setInputType(inputTypeText);
    }

    public static void setViewInputTypeNumber(View view) {
        int inputTypeNumber = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL;
        if (view instanceof EditText)
            ((EditText) view).setInputType(inputTypeNumber);
    }

    public static void setViewsText(List<View> views, List<String> viewsText) {
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            if (view instanceof EditText) {
                String val = viewsText.get(i);
                ((EditText) view).setText(val);
            } else if (view instanceof Spinner) {
                int val = Integer.valueOf(viewsText.get(i));
                ((Spinner) view).setSelection(val);
            } else if (view instanceof CheckBox) {
                boolean val = Boolean.valueOf(viewsText.get(i));
                ((CheckBox) view).setChecked(val);
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
        category.setTextColor(Color.BLACK);
        budget.setTypeface(null, Typeface.BOLD);
        budget.setTextSize(13);
        budget.setTextColor(Color.BLACK);
        balance.setTypeface(null, Typeface.BOLD);
        balance.setTextSize(13);
        balance.setTextColor(Color.BLACK);
    }
}
