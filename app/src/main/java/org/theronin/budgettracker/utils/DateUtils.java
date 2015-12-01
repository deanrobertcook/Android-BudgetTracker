package org.theronin.budgettracker.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static final SimpleDateFormat DATE_FORMAT_FOR_STORAGE = new SimpleDateFormat("yyyy-MM-dd");

    public static final double AVG_DAYS_IN_MONTH = 30.42;

    public static final int DAYS_IN_YEAR = 365;

    public static String getDisplayFormattedDate(long utcTime) {
        Locale locale = new Locale("en", "DE");
        SimpleDateFormat sdf;

        if (daysSince(utcTime) < DAYS_IN_YEAR) {
            sdf = new SimpleDateFormat("dd MMM.", locale);
        } else {
            sdf = new SimpleDateFormat("dd.MM.yy", locale);
        }
        return sdf.format(new Date(utcTime));
    }

    public static long getUtcTimeFromStorageFormattedDate(String formattedDate) {
        Date date = null;
        try {
            date = DATE_FORMAT_FOR_STORAGE.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date == null ? 0 : date.getTime();
    }

    public static String getStorageFormattedDate(long utcTime) {
        return DATE_FORMAT_FOR_STORAGE.format(new Date(utcTime));
    }

    public static String getStorageFormattedCurrentDate() {
        return getStorageFormattedDate(new Date().getTime());
    }

    public static long daysSince(long utcTime) {
        long diff = System.currentTimeMillis() - utcTime;
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }
}
