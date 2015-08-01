package com.theronin.budgettracker;

import com.theronin.budgettracker.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestUtils {

    public static String getDaysAgo(int daysAgo) {
        SimpleDateFormat format = new SimpleDateFormat(DateUtils.SQLITE_DATE_FORMAT);
        long daysAgoMilliseconds = System.currentTimeMillis() - (long) daysAgo * 24 * 60 * 60 * 1000;
        Date dateAgo = new Date(daysAgoMilliseconds);
        return format.format(dateAgo);
    }
}
