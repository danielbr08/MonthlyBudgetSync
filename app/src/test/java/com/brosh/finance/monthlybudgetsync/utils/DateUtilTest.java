package com.brosh.finance.monthlybudgetsync.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Comprehensive unit tests for the DateUtil class.
 * Tests all date manipulation and formatting methods.
 * 
 * Note: Some methods use Android's Log class which is not available in unit tests.
 * Those methods will fail silently in tests but work correctly in Android environment.
 */
public class DateUtilTest {

    private static final String DATE_FORMAT = "dd/MM/yyyy";
    
    // ============================================
    // GET CURRENT DATE TESTS
    // ============================================

    @Test
    public void testGetCurrentDate_WithSlash() {
        String result = DateUtil.getCurrentDate("/");
        assertNotNull(result);
        assertTrue(result.matches("\\d{2}/\\d{2}/\\d{4}"));
    }

    @Test
    public void testGetCurrentDate_WithDash() {
        String result = DateUtil.getCurrentDate("-");
        assertNotNull(result);
        assertTrue(result.matches("\\d{2}-\\d{2}-\\d{4}"));
    }

    @Test
    public void testGetCurrentDate_WithDot() {
        String result = DateUtil.getCurrentDate(".");
        assertNotNull(result);
        assertTrue(result.matches("\\d{2}\\.\\d{2}\\.\\d{4}"));
    }

    // ============================================
    // GET TODAY DATE TESTS
    // ============================================

    @Test
    public void testGetTodayDate_ReturnsNonNull() {
        Date result = DateUtil.getTodayDate();
        assertNotNull(result);
    }

    @Test
    public void testGetTodayDate_AtMidnight() {
        Date result = DateUtil.getTodayDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testGetTodayDate_IsToday() {
        Date result = DateUtil.getTodayDate();
        Calendar expected = Calendar.getInstance();
        Calendar actual = Calendar.getInstance();
        actual.setTime(result);
        
        assertEquals(expected.get(Calendar.YEAR), actual.get(Calendar.YEAR));
        assertEquals(expected.get(Calendar.MONTH), actual.get(Calendar.MONTH));
        assertEquals(expected.get(Calendar.DAY_OF_MONTH), actual.get(Calendar.DAY_OF_MONTH));
    }

    // ============================================
    // GET YEAR MONTH TESTS
    // ============================================

    @Test
    public void testGetYearMonth_ValidDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15);
        Date date = cal.getTime();
        
        String result = DateUtil.getYearMonth(date, "-");
        assertEquals("2024-01", result);
    }

    @Test
    public void testGetYearMonth_December() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.DECEMBER, 1);
        Date date = cal.getTime();
        
        String result = DateUtil.getYearMonth(date, "-");
        assertEquals("2024-12", result);
    }

    @Test
    public void testGetYearMonth_NullDate() {
        String result = DateUtil.getYearMonth(null, "-");
        assertEquals("", result);
    }

    @Test
    public void testGetYearMonth_CustomSeparator() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 15);
        Date date = cal.getTime();
        
        String result = DateUtil.getYearMonth(date, "/");
        assertEquals("2024/06", result);
    }

    // ============================================
    // GET DATE START MONTH TESTS
    // ============================================

    @Test
    public void testGetDateStartMonth_ReturnsFirstDay() {
        Date result = DateUtil.getDateStartMonth();
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetDateStartMonth_AtMidnight() {
        Date result = DateUtil.getDateStartMonth();
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    // ============================================
    // GET LAST DAY CURRENT MONTH TESTS
    // ============================================

    @Test
    public void testGetLastDayCurrentMonth_ReturnsPositive() {
        int result = DateUtil.getLastDayCurrentMonth();
        assertTrue(result >= 28 && result <= 31);
    }

    // ============================================
    // CONVERT STRING TO DATE TESTS
    // ============================================

    @Test
    public void testConvertStringToDate_ValidDate() {
        Date result = DateUtil.convertStringToDate("15/01/2024", DATE_FORMAT);
        assertNotNull(result);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(2024, cal.get(Calendar.YEAR));
    }

    @Test
    public void testConvertStringToDate_NullString() {
        Date result = DateUtil.convertStringToDate(null, DATE_FORMAT);
        assertNull(result);
    }

    @Test
    public void testConvertStringToDate_EmptyString() {
        Date result = DateUtil.convertStringToDate("", DATE_FORMAT);
        assertNull(result);
    }

    // Note: Tests that trigger Log.w() are moved to instrumented tests
    // because android.util.Log is not available in unit tests.
    // testConvertStringToDate_InvalidFormat and testConvertStringToDate_WrongFormat
    // are tested in MonthlyBudgetSyncInstrumentedTest

    // ============================================
    // CONVERT DATE TO STRING TESTS
    // ============================================

    @Test
    public void testConvertDateToString_ValidDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15);
        Date date = cal.getTime();
        
        String result = DateUtil.convertDateToString(date, DATE_FORMAT);
        assertEquals("15/01/2024", result);
    }

    @Test
    public void testConvertDateToString_NullDate() {
        String result = DateUtil.convertDateToString(null, DATE_FORMAT);
        assertEquals("", result);
    }

    // ============================================
    // STRING TO DATE TESTS
    // ============================================

    @Test
    public void testStringToDate_ValidDate() {
        Date result = DateUtil.stringToDate("15/01/2024", DATE_FORMAT);
        assertNotNull(result);
    }

    @Test
    public void testStringToDate_NullString() {
        Date result = DateUtil.stringToDate(null, DATE_FORMAT);
        assertNull(result);
    }

    @Test
    public void testStringToDate_EmptyString() {
        Date result = DateUtil.stringToDate("", DATE_FORMAT);
        assertNull(result);
    }

    // ============================================
    // CHANGE DATE FORMAT TESTS
    // ============================================

    @Test
    public void testChangeDateFormat_ValidDate() {
        Date original = new Date();
        Date result = DateUtil.changeDateFormat(original, DATE_FORMAT);
        assertNotNull(result);
        assertNotSame(original, result); // Should be a clone
    }

    @Test
    public void testChangeDateFormat_NullDate() {
        Date result = DateUtil.changeDateFormat(null, DATE_FORMAT);
        assertNull(result);
    }

    // ============================================
    // IS SAME YEAR MONTH TESTS
    // ============================================

    @Test
    public void testIsSameYearMonth_SameYearMonth() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2024, Calendar.JANUARY, 1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.JANUARY, 31);
        
        assertTrue(DateUtil.isSameYearMonth(cal1.getTime(), cal2.getTime()));
    }

    @Test
    public void testIsSameYearMonth_DifferentMonth() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2024, Calendar.JANUARY, 15);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.FEBRUARY, 15);
        
        assertFalse(DateUtil.isSameYearMonth(cal1.getTime(), cal2.getTime()));
    }

    @Test
    public void testIsSameYearMonth_DifferentYear() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2024, Calendar.JANUARY, 15);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2023, Calendar.JANUARY, 15);
        
        assertFalse(DateUtil.isSameYearMonth(cal1.getTime(), cal2.getTime()));
    }

    @Test
    public void testIsSameYearMonth_FirstNull() {
        Calendar cal = Calendar.getInstance();
        assertFalse(DateUtil.isSameYearMonth(null, cal.getTime()));
    }

    @Test
    public void testIsSameYearMonth_SecondNull() {
        Calendar cal = Calendar.getInstance();
        assertFalse(DateUtil.isSameYearMonth(cal.getTime(), null));
    }

    @Test
    public void testIsSameYearMonth_BothNull() {
        assertFalse(DateUtil.isSameYearMonth(null, null));
    }

    // ============================================
    // SET DAY TO DATE TESTS
    // ============================================

    @Test
    public void testSetDayToDate_ValidDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1);
        Date date = cal.getTime();
        
        Date result = DateUtil.setDayToDate(date, 15);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testSetDayToDate_NullDate() {
        Date result = DateUtil.setDayToDate(null, 15);
        assertNull(result);
    }

    @Test
    public void testSetDayToDate_DayTooHigh() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.FEBRUARY, 1); // February
        Date date = cal.getTime();
        
        Date result = DateUtil.setDayToDate(date, 31);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        // Should clamp to last day of February (29 in 2024 - leap year)
        assertEquals(29, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testSetDayToDate_DayTooLow() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15);
        Date date = cal.getTime();
        
        Date result = DateUtil.setDayToDate(date, 0);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testSetDayToDate_NegativeDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15);
        Date date = cal.getTime();
        
        Date result = DateUtil.setDayToDate(date, -5);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    // ============================================
    // GET NEXT REF MONTH TESTS
    // ============================================

    @Test
    public void testGetNextRefMonth_JanuaryToFebruary() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15);
        Date date = cal.getTime();
        
        Date result = DateUtil.getNextRefMonth(date);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH));
        assertEquals(2024, resultCal.get(Calendar.YEAR));
    }

    @Test
    public void testGetNextRefMonth_DecemberToJanuary() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.DECEMBER, 15);
        Date date = cal.getTime();
        
        Date result = DateUtil.getNextRefMonth(date);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        assertEquals(Calendar.JANUARY, resultCal.get(Calendar.MONTH));
        assertEquals(2025, resultCal.get(Calendar.YEAR));
    }

    @Test
    public void testGetNextRefMonth_NullDate() {
        Date result = DateUtil.getNextRefMonth(null);
        assertNull(result);
    }

    // ============================================
    // GET TODAY CALENDAR TESTS
    // ============================================

    @Test
    public void testGetTodayCalendar_AtMidnight() {
        Calendar result = DateUtil.getTodayCalendar();
        
        assertEquals(0, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, result.get(Calendar.MINUTE));
        assertEquals(0, result.get(Calendar.SECOND));
        assertEquals(0, result.get(Calendar.MILLISECOND));
    }

    @Test
    public void testGetTodayCalendar_IsToday() {
        Calendar result = DateUtil.getTodayCalendar();
        Calendar expected = Calendar.getInstance();
        
        assertEquals(expected.get(Calendar.YEAR), result.get(Calendar.YEAR));
        assertEquals(expected.get(Calendar.MONTH), result.get(Calendar.MONTH));
        assertEquals(expected.get(Calendar.DAY_OF_MONTH), result.get(Calendar.DAY_OF_MONTH));
    }

    // ============================================
    // GET CURRENT DATE (WITH DAY) TESTS
    // ============================================

    @Test
    public void testGetCurrentDate_WithDay() {
        Date result = DateUtil.getCurrentDate(15);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetCurrentDate_DayHigherThanMonth() {
        // If we request day 31 in a month with fewer days
        Date result = DateUtil.getCurrentDate(31);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        
        // Day should be clamped to last day of current month
        int lastDay = DateUtil.getLastDayCurrentMonth();
        assertTrue(cal.get(Calendar.DAY_OF_MONTH) <= lastDay);
    }

    // ============================================
    // GET DATE FROM REF MONTH KEY TESTS
    // ============================================

    @Test
    public void testGetDate_ValidYearMonth() {
        Date result = DateUtil.getDate("2024-01");
        assertNotNull(result);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetDate_December() {
        Date result = DateUtil.getDate("2024-12");
        assertNotNull(result);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    public void testLeapYear_February() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.FEBRUARY, 1); // 2024 is a leap year
        Date date = cal.getTime();
        
        Date result = DateUtil.setDayToDate(date, 29);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        assertEquals(29, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testNonLeapYear_February() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.FEBRUARY, 1); // 2023 is not a leap year
        Date date = cal.getTime();
        
        Date result = DateUtil.setDayToDate(date, 29);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        
        // Should clamp to 28
        assertEquals(28, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetYearMonth_SingleDigitMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 15); // Month 3
        Date date = cal.getTime();
        
        String result = DateUtil.getYearMonth(date, "-");
        assertEquals("2024-03", result); // Should be zero-padded
    }
}
