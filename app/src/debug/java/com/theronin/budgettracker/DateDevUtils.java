package com.theronin.budgettracker;

import com.theronin.budgettracker.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class DateDevUtils {

    public static String getDaysAgo(int daysAgo) {
        SimpleDateFormat format = new SimpleDateFormat(DateUtils.SQLITE_DATE_FORMAT);
        long daysAgoMilliseconds = System.currentTimeMillis() - (long) daysAgo * 24 * 60 * 60 * 1000;
        Date dateAgo = new Date(daysAgoMilliseconds);
        return format.format(dateAgo);
    }

    public static String getRandomDate() {
        //Return a random date from within the last five years
        return getDaysAgo(new Random().nextInt(10000));
    }

}
