package com.brosh.finance.monthlybudgetsync.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

/**
 * Utility class for UI operations.
 * Provides helper methods for view styling, toolbar setup, advertisements, and more.
 */
public final class UiUtil {

    // ============================================
    // CONSTANTS
    // ============================================
    
    private static final int HEADER_TEXT_SIZE = 15;
    private static final int TOTAL_ROW_TEXT_SIZE = 13;
    private static final int MAX_DAYS_IN_MONTH = 31;

    // ============================================
    // CONSTRUCTOR
    // ============================================
    
    private UiUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    // ============================================
    // TOOLBAR SETUP
    // ============================================

    /**
     * Sets up the toolbar with custom styling and title.
     * 
     * @param activity the activity to set up toolbar for
     * @param yearMonth optional year-month string to display in title
     */
    public static void setToolbar(@NonNull AppCompatActivity activity, @Nullable String yearMonth) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.action_bar_layout);
            actionBar.setBackgroundDrawable(
                    new ColorDrawable(ContextCompat.getColor(activity, R.color.colorApp)));
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        setTitleText(activity, yearMonth);
    }

    /**
     * Sets the title text in the action bar.
     */
    private static void setTitleText(@NonNull AppCompatActivity activity, @Nullable String yearMonth) {
        String title = activity.getString(R.string.app_name);
        if (yearMonth != null) {
            title += "\n" + yearMonth;
        }
        
        TextView tvTitle = activity.findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    // ============================================
    // ADVERTISEMENTS
    // ============================================

    /**
     * Initializes and loads an ad banner in the activity.
     * 
     * @param context the activity context
     */
    public static void addAdvertiseToActivity(@NonNull Context context) {
        MobileAds.initialize(context, initializationStatus -> { });
        
        AdView adView = ((Activity) context).findViewById(R.id.adView);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    // ============================================
    // TEXT STYLING
    // ============================================

    /**
     * Applies header styling to a TextView.
     * 
     * @param tv the TextView to style
     * @param textSize the text size
     * @param clickable whether the view should be clickable with linkify
     */
    public static void setHeaderProperties(@NonNull TextView tv, int textSize, boolean clickable) {
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(textSize);
        tv.setClickable(clickable);
        
        if (clickable) {
            Linkify.addLinks(tv, Linkify.ALL);
        }
    }

    /**
     * Applies header styling to multiple TextViews.
     */
    public static void setHeaderProperties(@NonNull List<TextView> textViews, int textSize, boolean clickable) {
        for (TextView tv : textViews) {
            setHeaderProperties(tv, textSize, clickable);
        }
    }

    /**
     * Sets text color for multiple TextViews.
     * 
     * @param textViews the TextViews to style
     * @param color the color to set
     */
    public static void setTextViewColor(@NonNull List<TextView> textViews, int color) {
        for (TextView tv : textViews) {
            tv.setTextColor(color);
        }
    }

    /**
     * Applies total row styling to TextViews.
     * 
     * @param totalRow the TextViews in the total row (category, budget, balance)
     */
    public static void setTotalBudgetRow(@NonNull List<TextView> totalRow) {
        for (TextView tv : totalRow) {
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextSize(TOTAL_ROW_TEXT_SIZE);
        }
        setTextViewColor(totalRow, Color.BLACK);
    }

    // ============================================
    // STRIKE-THROUGH TEXT
    // ============================================

    /**
     * Adds strike-through to a TextView.
     */
    public static void strikeThroughText(@NonNull TextView tv) {
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    /**
     * Removes strike-through from a TextView.
     */
    public static void unStrikeThroughText(@NonNull TextView tv) {
        tv.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
    }

    /**
     * Sets strike-through state for multiple TextViews.
     * 
     * @param textViews the TextViews to modify
     * @param enabled true to enable strike-through, false to remove
     */
    public static void strikeThroughText(@NonNull List<TextView> textViews, boolean enabled) {
        for (TextView tv : textViews) {
            if (enabled) {
                strikeThroughText(tv);
            } else {
                unStrikeThroughText(tv);
            }
        }
    }

    // ============================================
    // TEXT SIZE
    // ============================================

    /**
     * Sets text size for a view (TextView, EditText, or CheckBox).
     */
    public static void setTxtSize(@NonNull View view, float size) {
        if (view instanceof TextView textView) {
            textView.setTextSize(size);
        } else if (view instanceof EditText editText) {
            editText.setTextSize(size);
        } else if (view instanceof CheckBox checkBox) {
            checkBox.setTextSize(size);
        }
    }

    /**
     * Sets text size for multiple views.
     */
    public static void setTxtSize(@NonNull List<View> views, float size) {
        for (View view : views) {
            setTxtSize(view, size);
        }
    }

    // ============================================
    // INPUT TYPES
    // ============================================

    /**
     * Sets input type to text for a view.
     */
    public static void setViewInputTypeText(@NonNull View view) {
        int inputTypeText = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
        
        if (view instanceof EditText editText) {
            editText.setInputType(inputTypeText);
        } else if (view instanceof CheckBox checkBox) {
            checkBox.setInputType(inputTypeText);
        }
    }

    /**
     * Sets input type to number for a view.
     */
    public static void setViewInputTypeNumber(@NonNull View view) {
        int inputTypeNumber = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL;
        
        if (view instanceof EditText editText) {
            editText.setInputType(inputTypeNumber);
        }
    }

    // ============================================
    // VIEW CONTENT
    // ============================================

    /**
     * Sets text/values for multiple views based on their type.
     * Supports EditText, Spinner, and CheckBox.
     * 
     * @param views the views to set values for
     * @param viewsText the text values to set
     */
    public static void setViewsText(@NonNull List<View> views, @NonNull List<String> viewsText) {
        int size = Math.min(views.size(), viewsText.size());
        
        for (int i = 0; i < size; i++) {
            View view = views.get(i);
            String value = viewsText.get(i);
            
            setViewContent(view, value);
        }
    }

    /**
     * Sets content for a single view based on its type.
     */
    private static void setViewContent(@NonNull View view, @Nullable String value) {
        if (view instanceof EditText editText) {
            editText.setText(value);
        } else if (view instanceof Spinner spinner) {
            int selection = parseIntSafe(value, 0);
            spinner.setSelection(selection);
        } else if (view instanceof CheckBox checkBox) {
            boolean checked = Boolean.parseBoolean(value);
            checkBox.setChecked(checked);
        }
    }

    /**
     * Safely parses an integer from a string.
     */
    private static int parseIntSafe(@Nullable String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Requests focus for a view.
     */
    public static void setInputFocus(@NonNull View view) {
        view.requestFocus();
    }

    // ============================================
    // LAYOUT HELPERS
    // ============================================

    /**
     * Sets layout widths for create budget page widgets.
     * Widgets order: categoryNameET, categoryValueET, constPaymentCB, shopET, optionalDaysSpinner
     */
    public static void setWidthCreateBudgetPageDataWidgets(@NonNull List<View> widgets, 
                                                           int screenWidthReduceButtonSize, 
                                                           int wrapContent) {
        if (widgets.size() < 5) return;
        
        setLayoutWidth(widgets.get(0), screenWidthReduceButtonSize, Config.CATEGORY_NAME_ET_WIDTH_PERCENT, wrapContent);
        setLayoutWidth(widgets.get(1), screenWidthReduceButtonSize, Config.CATEGORY_VALUE_ET_WIDTH_PERCENT, wrapContent);
        setLayoutWidth(widgets.get(2), screenWidthReduceButtonSize, Config.CONST_PAYMENT_CB_WIDTH_PERCENT, wrapContent);
        setLayoutWidth(widgets.get(3), screenWidthReduceButtonSize, Config.SHOP_ET_WIDTH_PERCENT, wrapContent);
        setLayoutWidth(widgets.get(4), screenWidthReduceButtonSize, Config.OPTIONAL_DAYS_SPINNER_WIDTH_PERCENT, wrapContent);
    }

    /**
     * Sets layout width for a single view.
     */
    private static void setLayoutWidth(@NonNull View view, int totalWidth, double percent, int height) {
        int width = (int) Math.floor(totalWidth * percent);
        view.setLayoutParams(new LinearLayout.LayoutParams(width, height));
    }

    /**
     * Reverses the order of children in a LinearLayout.
     */
    @SuppressWarnings("unused")
    public static void reverseLinearLayout(@NonNull LinearLayout linearLayout) {
        for (int i = linearLayout.getChildCount() - 1; i >= 0; i--) {
            View item = linearLayout.getChildAt(i);
            linearLayout.removeViewAt(i);
            linearLayout.addView(item);
        }
    }

    // ============================================
    // VIEW FINDING
    // ============================================

    /**
     * Recursively finds all TextViews in a ViewGroup.
     * 
     * @param viewGroup the root ViewGroup to search
     * @return list of all TextViews found
     */
    @NonNull
    public static List<View> findAllTextviews(@NonNull ViewGroup viewGroup) {
        List<View> textViews = new ArrayList<>();
        
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            
            if (view instanceof ViewGroup vg) {
                textViews.addAll(findAllTextviews(vg));
            } else if (view instanceof TextView) {
                textViews.add(view);
            }
        }
        
        return textViews;
    }

    /**
     * Finds TextView IDs that have the specified text.
     * 
     * @param viewGroup the root ViewGroup to search
     * @param keyName the text to match
     * @return list of matching view IDs
     */
    @NonNull
    public static List<Integer> getIdTVByName(@NonNull ViewGroup viewGroup, @NonNull String keyName) {
        List<Integer> textViewIds = new ArrayList<>();
        
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            
            if (view instanceof ViewGroup vg) {
                textViewIds.addAll(getIdTVByName(vg, keyName));
            } else if (view instanceof TextView textView) {
                if (keyName.equals(textView.getText().toString())) {
                    textViewIds.add(view.getId());
                }
            }
        }
        
        return textViewIds;
    }

    // ============================================
    // BACKGROUND HELPERS
    // ============================================

    /**
     * Restores background drawable for multiple views.
     * 
     * @param views the views to restore
     * @param drawable the background drawable to set
     */
    public static void restoreBackground(@NonNull List<View> views, @Nullable Drawable drawable) {
        for (View v : views) {
            v.setBackground(drawable);
        }
    }

    // ============================================
    // SPINNER HELPERS
    // ============================================

    /**
     * Sets up a spinner with days of the month (1-30).
     * 
     * @param spinner the spinner to set up
     * @param activity the activity context
     */
    public static void setDaysInMonthSpinner(@NonNull Spinner spinner, @NonNull Activity activity) {
        List<Integer> daysInMonth = IntStream.range(1, MAX_DAYS_IN_MONTH)
                .boxed()
                .collect(Collectors.toList());
        List<String> daysAsStrings = Lists.transform(daysInMonth, Functions.toStringFunction());
        
        SpinnerAdapter adapter = new SpinnerAdapter(daysAsStrings, activity, R.layout.custom_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(1, true);
        spinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    // ============================================
    // TITLE WIDGET HELPERS (Legacy)
    // ============================================

    /**
     * Sets layout widths for title widgets.
     */
    @SuppressWarnings("unused")
    public static void setWidthCreateBudgetPageTitleWidgets(@NonNull List<View> widgets, 
                                                            int screenWidthReduceButtonSize, 
                                                            int wrapContent) {
        if (widgets.size() < 5) return;
        
        setLayoutWidth(widgets.get(0), screenWidthReduceButtonSize, Config.CATEGORY_NAME_TV_TITLE_WIDTH_PERCENT, wrapContent);
        setLayoutWidth(widgets.get(1), screenWidthReduceButtonSize, Config.CATEGORY_VALUE_TV_TITLE_WIDTH_PERCENT, wrapContent);
        setLayoutWidth(widgets.get(2), screenWidthReduceButtonSize, Config.CONST_PAYMENT_TV_TITLE_WIDTH_PERCENT, wrapContent);
        setLayoutWidth(widgets.get(3), screenWidthReduceButtonSize, Config.SHOP_TV_TITLE_WIDTH_PERCENT, wrapContent);
        setLayoutWidth(widgets.get(4), screenWidthReduceButtonSize, Config.PAY_DATE_TITLE_WIDTH_PERCENT, wrapContent);
    }

    /**
     * Sets text for title widgets with capitalized first letters.
     */
    @SuppressWarnings("unused")
    public static void setTextTitleWidgets(@NonNull List<View> widgets, @NonNull List<String> titlesNames) {
        int size = Math.min(widgets.size(), titlesNames.size());
        
        for (int i = 0; i < size; i++) {
            View view = widgets.get(i);
            if (view instanceof TextView textView) {
                textView.setText(TextUtil.getWordCapitalLetter(titlesNames.get(i)));
            }
        }
    }

    /**
     * Applies title styling to TextViews.
     */
    @SuppressWarnings("unused")
    public static void setTitleStyle(@NonNull List<TextView> titlesTV) {
        for (TextView tv : titlesTV) {
            setHeaderProperties(tv, HEADER_TEXT_SIZE, true);
        }
    }

    /**
     * Applies strike-through to a row of TextViews.
     */
    @SuppressWarnings("unused")
    public static void setRowStrikeThroughStyle(@NonNull List<TextView> tvRow) {
        for (TextView tv : tvRow) {
            strikeThroughText(tv);
        }
    }

    // ============================================
    // DIALOG HELPERS (Legacy - use DialogHelper instead)
    // ============================================

    /**
     * @deprecated Use DialogHelper.showTextInputDialog instead
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static void openShareDialog(AlertDialog.Builder builder, 
                                       AlertDialog dialog, 
                                       Context context, 
                                       String hint, 
                                       String title, 
                                       int inputType,
                                       EditText editTextInput, 
                                       String positiveText, 
                                       String negativeText, 
                                       View.OnClickListener onClickListener) {
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
