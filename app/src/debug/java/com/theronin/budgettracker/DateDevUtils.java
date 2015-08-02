package com.theronin.budgettracker;

import java.util.Date;
import java.util.Random;

import static com.theronin.budgettracker.utils.DateUtils.DATE_FORMAT_FOR_STORAGE;

public class DateDevUtils {

    public static String getDaysAgo(int daysAgo) {
        long daysAgoMilliseconds = System.currentTimeMillis() - (long) daysAgo * 24 * 60 * 60 * 1000;
        Date dateAgo = new Date(daysAgoMilliseconds);
        return DATE_FORMAT_FOR_STORAGE.format(dateAgo);
    }

    public static String getRandomDate() {
        //Return a random date from within the last five years
        return getDaysAgo(new Random().nextInt(2000));
    }

}
