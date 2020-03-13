package com.brosh.finance.monthlybudgetsync.services;

import android.graphics.*;
import android.os.Build;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.RequiresApi;

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void setLanguageConf(LinearLayout l) {
        for (int i = 0; i < l.getChildCount(); i++) {
            View v = l.getChildAt(i);
            v.setTextDirection(View.TEXT_DIRECTION_LTR);
            v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
        reverseLinearLayout(l);
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

    public static void setTextTitleWidgets(List<View> widgets) {
        // Widgets are order by : categoryNameET, categoryValueET, constPaymentCB, shopET, payDateTV
        Language language = new Language(Config.DEFAULT_LANGUAGE);
        int i = 0;
        ((TextView) widgets.get(i++)).setText(TextService.getWordCapitalLetter(language.categoryName));
        ((TextView) widgets.get(i++)).setText(TextService.getWordCapitalLetter(language.budgetName));
        ((TextView) widgets.get(i++)).setText(TextService.getWordCapitalLetter(language.constantDate));
        ((TextView) widgets.get(i++)).setText(TextService.getWordCapitalLetter(language.shopName));
        ((TextView) widgets.get(i)).setText(TextService.getWordCapitalLetter(language.chargeDay));
    }

    public static void setTitleStyle(List<TextView> titlesTV, LinearLayout titleLL) {
        for (TextView titletv : titlesTV) {
            setHeaderProperties(titletv);
            titleLL.addView(titletv);
        }
    }

    public static void setRowStrikeThroughStyle(List<TextView> tvRow) {
        for (TextView tv : tvRow) {
            strikeThroughText(tv);
        }
    }
}
