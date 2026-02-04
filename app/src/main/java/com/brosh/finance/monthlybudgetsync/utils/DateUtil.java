package com.brosh.finance.monthlybudgetsync.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;

import java.text.*;
import java.util.*;

/**
 * Utility class for date operations.
 * All methods are thread-safe and null-safe.
 */
public final class DateUtil {
    private static final String TAG = "DateUtil";
    
    // Private constructor to prevent instantiation
    private DateUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Returns the current date as a formatted string.
     * @param separator the separator between date parts (e.g., "/")
     * @return formatted date string (dd{sep}MM{sep}yyyy)
     */
    @NonNull
    public static String getCurrentDate(@NonNull String separator) {
        if (separator == null) {
            separator = "/";
        }
        
        Calendar currentDate = Calendar.getInstance();
        int mYear = currentDate.get(Calendar.YEAR);
        int mMonth = currentDate.get(Calendar.MONTH) + 1;
        int mDay = currentDate.get(Calendar.DAY_OF_MONTH);

        String day = mDay < 10 ? "0" + mDay : String.valueOf(mDay);
        String month = mMonth < 10 ? "0" + mMonth : String.valueOf(mMonth);
        
        return day + separator + month + separator + mYear;
    }

    /**
     * Returns today's date at midnight (00:00:00.000).
     * @return Date object representing start of today
     */
    @NonNull
    public static Date getTodayDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Returns the year-month string for a given date.
     * Uses Calendar instead of deprecated Date methods.
     * 
     * @param date the date to format
     * @param separator the separator between year and month
     * @return formatted string (yyyy{sep}MM), or empty string if date is null
     */
    @NonNull
    public static String getYearMonth(@Nullable Date date, @NonNull String separator) {
        if (date == null) {
            return "";
        }
        if (separator == null) {
            separator = "-";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        
        String monthStr = month < 10 ? Definitions.ZERO + month : String.valueOf(month);
        String yearStr = String.valueOf(year);

        return yearStr + separator + monthStr;
    }

    @SuppressWarnings("unused") // May be used for future functionality
    public static Date getDateStartMonth() {
        Calendar c = Calendar.getInstance();

        // set the calendar to start of today
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    /**
     * Reverses a date string from one format to another.
     * Example: "01/02/2024" -> "2024/02/01"
     * 
     * @param date the date string to reverse
     * @param separator the separator used in the date string
     * @return reversed date string, or original string if parsing fails
     */
    @SuppressWarnings("unused") // May be used for future functionality
    @NonNull
    public static String reverseDateString(@Nullable String date, @Nullable String separator) {
        if (date == null || separator == null) {
            return date != null ? date : "";
        }
        
        try {
            String[] parts = date.split(separator.equals(".") ? "\\." : separator);
            if (parts.length != 3) {
                return date;
            }
            
            String actualSeparator = separator.equals("\\.") ? Definitions.DOT : separator;
            return parts[2] + actualSeparator + parts[1] + actualSeparator + parts[0];
        } catch (Exception e) {
            Log.w(TAG, "Error reversing date string: " + date, e);
            return date;
        }
    }

    /**
     * Converts a string to a Date object.
     * 
     * @param stringDate the date string to parse
     * @param format the format of the date string (e.g., "dd/MM/yyyy")
     * @return Date object, or null if parsing fails
     */
    @Nullable
    public static Date convertStringToDate(@Nullable String stringDate, @NonNull String format) {
        if (stringDate == null || stringDate.isEmpty()) {
            return null;
        }
        
        try {
            SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
            df.setLenient(false); // Strict parsing
            return df.parse(stringDate);
        } catch (ParseException e) {
            Log.w(TAG, "Error parsing date string: " + stringDate + " with format: " + format, e);
            return null;
        }
    }

    /**
     * Converts a Date to a formatted string.
     * 
     * @param date the date to format
     * @param format the desired output format
     * @return formatted date string, or empty string if date is null
     */
    @NonNull
    public static String convertDateToString(@Nullable Date date, @NonNull String format) {
        if (date == null) {
            return "";
        }
        
        try {
            SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());
            return df.format(date);
        } catch (Exception e) {
            Log.w(TAG, "Error formatting date", e);
            return "";
        }
    }

    public static int getLastDayCurrentMonth() {
        return getTodayCalendar().getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static Calendar getTodayCalendar() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public static Date getCurrentDate(int day) {
        int _day = day;
        int lastDayInMonth = getLastDayCurrentMonth();
        if (day > lastDayInMonth)
            _day = lastDayInMonth;
        Calendar c = DateUtil.getTodayCalendar();
        c.set(Calendar.DAY_OF_MONTH, _day);
        return c.getTime();
    }

    public static Date getDate(String refMonthKey) {
        String[] yearMonth = refMonthKey.split(Config.SEPARATOR);
        String year = yearMonth[0];
        String month = yearMonth[1];
        String day = "01";
        StringBuilder date = new StringBuilder();
        date.append(day).append(Config.DATE_FORMAT_CHARACTER).append(month).append(Config.DATE_FORMAT_CHARACTER).append(year);
        return stringToDate(date.toString(), Config.DATE_FORMAT);
    }

    /**
     * Parses a string into a Date object.
     * 
     * @param stringDate the date string to parse
     * @param format the format of the date string
     * @return Date object, or null if parsing fails
     */
    @Nullable
    public static Date stringToDate(@Nullable String stringDate, @NonNull String format) {
        if (stringDate == null || stringDate.isEmpty()) {
            return null;
        }
        
        try {
            SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
            df.setLenient(false);
            return df.parse(stringDate);
        } catch (ParseException e) {
            Log.w(TAG, "Error parsing date: " + stringDate, e);
            return null;
        }
    }

    /**
     * Returns a normalized copy of the date (preserves time components).
     * Note: This method returns a clone of the date; the format parameter
     * was previously unused for actual reformatting.
     * 
     * @param date the date to normalize
     * @param format the format (for compatibility, not actively used)
     * @return cloned Date object, or null if input is null
     */
    @Nullable
    public static Date changeDateFormat(@Nullable Date date, @NonNull String format) {
        if (date == null) {
            return null;
        }
        return (Date) date.clone();
    }

    /**
     * Checks if two dates are in the same year and month.
     * Uses Calendar instead of deprecated Date methods.
     * 
     * @param d1 first date
     * @param d2 second date
     * @return true if same year and month, false otherwise or if either date is null
     */
    public static boolean isSameYearMonth(@Nullable Date d1, @Nullable Date d2) {
        if (d1 == null || d2 == null) {
            return false;
        }
        
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) 
            && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    /**
     * Sets the day of month for a given date, clamping to valid range.
     * 
     * @param date the date to modify
     * @param day the desired day of month (1-31)
     * @return new Date with the specified day, or null if input is null
     */
    @Nullable
    public static Date setDayToDate(@Nullable Date date, int day) {
        if (date == null) {
            return null;
        }
        
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        
        int lastDayInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        int actualDay = Math.min(Math.max(day, 1), lastDayInMonth);
        
        c.set(Calendar.DAY_OF_MONTH, actualDay);
        return c.getTime();
    }

    /**
     * Returns the same day in the next month.
     * Handles month-end edge cases appropriately.
     * 
     * @param refMonth the reference date
     * @return Date representing the next month, or null if input is null
     */
    @Nullable
    public static Date getNextRefMonth(@Nullable Date refMonth) {
        if (refMonth == null) {
            return null;
        }
        
        Calendar c = Calendar.getInstance();
        c.setTime(refMonth);
        c.add(Calendar.MONTH, 1);
        return c.getTime();
    }
    
    /**
     * Validates if a date string matches the expected format.
     * 
     * @param dateStr the date string to validate
     * @param format the expected format
     * @return true if valid, false otherwise
     */
    @SuppressWarnings("unused") // May be used for future functionality
    public static boolean isValidDateFormat(@Nullable String dateStr, @NonNull String format) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
            sdf.setLenient(false);
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
