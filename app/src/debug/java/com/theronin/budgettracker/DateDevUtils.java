package com.theronin.budgettracker;

import com.theronin.budgettracker.utils.DateUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import static com.theronin.budgettracker.utils.DateUtils.DATE_FORMAT_FOR_STORAGE;

public class DateDevUtils {

    public static String getDaysAgo(int daysAgo) {
        long daysAgoMilliseconds = System.currentTimeMillis() - (long) daysAgo * 24 * 60 * 60 * 1000;
        Date dateAgo = new Date(daysAgoMilliseconds);
        return DATE_FORMAT_FOR_STORAGE.format(dateAgo);
    }

    public static String getDaysBefore(String date, int daysBefore) {
        String returnDate = null;
        try {
            long dateInMillis = DateUtils.DATE_FORMAT_FOR_STORAGE.parse(date).getTime();
            long daysBeforeInMillis = dateInMillis - (long) daysBefore * 24 * 60 * 60 * 1000;
            returnDate = DateUtils.getStorageFormattedDate(new Date(daysBeforeInMillis));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
    }

    public static String getRandomDate() {
        //Return a random date from within the last five years
        return getDaysAgo(new Random().nextInt(2000));
    }

}
