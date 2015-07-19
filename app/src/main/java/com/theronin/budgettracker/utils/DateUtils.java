package com.theronin.budgettracker.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static final String SQLITE_DATE_FORMAT = "yyyy-MM-dd";

    public static String formatDate(long utcDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(utcDate);
        return calendar.get(Calendar.YEAR) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar
                .get(Calendar.DAY_OF_MONTH);
    }

    public static String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat(SQLITE_DATE_FORMAT);
        Date now = new Date();
        String strDate = sdf.format(now);
        return strDate;
    }
}
