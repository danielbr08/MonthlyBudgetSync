package com.brosh.finance.monthlybudgetsync.services;

import com.brosh.finance.monthlybudgetsync.config.Definition;
import com.brosh.finance.monthlybudgetsync.services.Language;

import java.text.*;
import java.util.*;


public final class DateService {
    public static Date getTodayDate(){
        Calendar c = Calendar.getInstance();

        // set the calendar to start of today
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static String getYearMonth(Date date, String separator) {
        int month = date.getMonth() + 1;
        String monthStr = String.valueOf(month);
        if(month < 10)
            monthStr = Definition.ZERO + month;

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        String yearStr = String.valueOf(year);

        return yearStr + separator + monthStr;
    }

    public static Date getDateStartMonth()
    {
        Calendar c = Calendar.getInstance();

        // set the calendar to start of today
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    //  Input: String with date include day
    public static String reverseDateString(String date, String separator) {
        String[] l = date.split(separator);
        if(separator == "\\.")// todo add this string to Definition
            separator = Definition.DOT;
        return l[2] + separator + l[1] + separator + l[0];
    }

    public static Date convertStringToDate(String stringDate, String format){ // "dd/MM/yyyy"
        //String lastTimeDateString = "06/27/2017";
        java.text.DateFormat df = new SimpleDateFormat(format, Locale.US);
        Date date = null;
        try {
            date = df.parse(stringDate);
            String newStringDate = df.format(date);
            System.out.println(newStringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // need end of month
        return date;
    }

    public static String convertDateToString(Date date, String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(date);
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

    public static Date getCurrentDate(int day){
        int _day = day;
        int lastDayInMonth = getLastDayCurrentMonth();
        if(day > lastDayInMonth)
            _day = lastDayInMonth;
        Calendar c = DateService.getTodayCalendar();
        c.set(Calendar.DAY_OF_MONTH, _day);
        return c.getTime();
    }

}
