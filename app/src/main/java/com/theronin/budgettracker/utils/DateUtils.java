package com.theronin.budgettracker.utils;

import java.util.Calendar;

public class DateUtils {
    public static String formatDate(long utcDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(utcDate);
        return calendar.get(Calendar.YEAR) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar
                .get(Calendar.DAY_OF_MONTH);
    }
}
