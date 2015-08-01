package com.theronin.budgettracker.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static final String SQLITE_DATE_FORMAT = "yyyy-MM-dd";
    public static final double AVG_DAYS_IN_MONTH = 30.42;


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

    public static long daysSince(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startDate = format.parse(date);
            Date today = new Date(System.currentTimeMillis());
            long diff = today.getTime() - startDate.getTime();
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
