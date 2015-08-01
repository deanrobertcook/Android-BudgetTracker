package com.theronin.budgettracker.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static final SimpleDateFormat DATE_FORMAT_FOR_STORAGE = new SimpleDateFormat("yyyy-MM-dd");

    public static final double AVG_DAYS_IN_MONTH = 30.42;

    public static final int DAYS_IN_YEAR = 365;

    public static String getDisplayFormattedDate(Date date) {
        Locale locale = new Locale("en", "DE");
        SimpleDateFormat sdf;

        if (daysSince(date) < DAYS_IN_YEAR) {
            sdf = new SimpleDateFormat("dd MMM.", locale);
        } else {
            sdf = new SimpleDateFormat("dd.MM.yy", locale);
        }
        return sdf.format(date);
    }

    public static String getDisplayFormattedDate(String storageFormattedDate) {
        try {
            Date date = DATE_FORMAT_FOR_STORAGE.parse(storageFormattedDate);
            return getDisplayFormattedDate(date);
        } catch (ParseException e) {
            throw new RuntimeException("The date formatter expected a date of the format " + DATE_FORMAT_FOR_STORAGE.toPattern());
        }
    }

    public static String getStorageFormattedDate(Date date) {
        return DATE_FORMAT_FOR_STORAGE.format(date);
    }

    public static String getStorageFormattedCurrentDate() {
        return getStorageFormattedDate(new Date());
    }

    public static long daysSince(String date) {
        try {
            Date startDate = DATE_FORMAT_FOR_STORAGE.parse(date);
            return daysSince(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static long daysSince(Date date) {
        Date today = new Date(System.currentTimeMillis());
        long diff = today.getTime() - date.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }


}
