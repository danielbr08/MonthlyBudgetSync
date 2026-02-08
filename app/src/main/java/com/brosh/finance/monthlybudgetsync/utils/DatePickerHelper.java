package com.brosh.finance.monthlybudgetsync.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.config.Config;

import java.util.Calendar;
import java.util.Date;

/**
 * Helper class for DatePicker dialogs.
 * Provides consistent date picker behavior across the app.
 */
public final class DatePickerHelper {
    
    private DatePickerHelper() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    /**
     * Callback interface for date selection.
     */
    public interface OnDateSelectedListener {
        /**
         * Called when a date is selected.
         * 
         * @param year the selected year
         * @param month the selected month (0-based)
         * @param dayOfMonth the selected day
         * @param formattedDate the date formatted as string
         */
        void onDateSelected(int year, int month, int dayOfMonth, @NonNull String formattedDate);
    }
    
    /**
     * Shows a date picker dialog and populates an EditText with the result.
     * 
     * @param context the context
     * @param editText the EditText to populate
     */
    public static void showDatePicker(@NonNull Context context, @NonNull EditText editText) {
        showDatePicker(context, editText, null);
    }
    
    /**
     * Shows a date picker dialog with a custom title.
     * 
     * @param context the context
     * @param editText the EditText to populate
     * @param title the dialog title (null for default)
     */
    public static void showDatePicker(@NonNull Context context, 
                                      @NonNull EditText editText,
                                      @Nullable String title) {
        // Hide keyboard first
        KeyboardUtil.hideKeyboard(context, editText);
        
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog dialog = new DatePickerDialog(context,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = formatDate(selectedDay, selectedMonth + 1, selectedYear);
                editText.setText(formattedDate);
                editText.setError(null);
            }, year, month, day);
        
        if (title != null) {
            dialog.setTitle(title);
        } else {
            dialog.setTitle(R.string.selecting_date);
        }
        
        dialog.show();
    }
    
    /**
     * Shows a date picker dialog with callback.
     * 
     * @param context the context
     * @param listener callback for date selection
     */
    public static void showDatePicker(@NonNull Context context,
                                      @NonNull OnDateSelectedListener listener) {
        showDatePicker(context, null, listener);
    }
    
    /**
     * Shows a date picker dialog with title and callback.
     * 
     * @param context the context
     * @param title the dialog title (null for default)
     * @param listener callback for date selection
     */
    public static void showDatePicker(@NonNull Context context,
                                      @Nullable String title,
                                      @NonNull OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog dialog = new DatePickerDialog(context,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = formatDate(selectedDay, selectedMonth + 1, selectedYear);
                listener.onDateSelected(selectedYear, selectedMonth, selectedDay, formattedDate);
            }, year, month, day);
        
        if (title != null) {
            dialog.setTitle(title);
        }
        
        dialog.show();
    }
    
    /**
     * Shows a date picker with a pre-selected date.
     * 
     * @param context the context
     * @param initialDate the initial date to show
     * @param editText the EditText to populate
     */
    public static void showDatePickerWithInitialDate(@NonNull Context context,
                                                     @NonNull Date initialDate,
                                                     @NonNull EditText editText) {
        KeyboardUtil.hideKeyboard(context, editText);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(initialDate);
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog dialog = new DatePickerDialog(context,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = formatDate(selectedDay, selectedMonth + 1, selectedYear);
                editText.setText(formattedDate);
                editText.setError(null);
            }, year, month, day);
        
        dialog.setTitle(R.string.selecting_date);
        dialog.show();
    }
    
    /**
     * Shows a date picker with min and max date constraints.
     * 
     * @param context the context
     * @param editText the EditText to populate
     * @param minDate minimum selectable date (null for no minimum)
     * @param maxDate maximum selectable date (null for no maximum)
     */
    public static void showDatePickerWithConstraints(@NonNull Context context,
                                                     @NonNull EditText editText,
                                                     @Nullable Date minDate,
                                                     @Nullable Date maxDate) {
        KeyboardUtil.hideKeyboard(context, editText);
        
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog dialog = new DatePickerDialog(context,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = formatDate(selectedDay, selectedMonth + 1, selectedYear);
                editText.setText(formattedDate);
                editText.setError(null);
            }, year, month, day);
        
        if (minDate != null) {
            dialog.getDatePicker().setMinDate(minDate.getTime());
        }
        
        if (maxDate != null) {
            dialog.getDatePicker().setMaxDate(maxDate.getTime());
        }
        
        dialog.setTitle(R.string.selecting_date);
        dialog.show();
    }
    
    // ============================================
    // FORMATTING HELPERS
    // ============================================
    
    /**
     * Formats a date in DD/MM/YYYY format.
     * 
     * @param day the day
     * @param month the month (1-based)
     * @param year the year
     * @return formatted date string
     */
    @NonNull
    public static String formatDate(int day, int month, int year) {
        String dayStr = day < 10 ? "0" + day : String.valueOf(day);
        String monthStr = month < 10 ? "0" + month : String.valueOf(month);
        return dayStr + Config.DATE_FORMAT_CHARACTER + monthStr + Config.DATE_FORMAT_CHARACTER + year;
    }
    
    /**
     * Formats a date in YYYY-MM format (year-month).
     * 
     * @param month the month (1-based)
     * @param year the year
     * @return formatted year-month string
     */
    @NonNull
    public static String formatYearMonth(int month, int year) {
        String monthStr = month < 10 ? "0" + month : String.valueOf(month);
        return year + Config.SEPARATOR + monthStr;
    }
}
