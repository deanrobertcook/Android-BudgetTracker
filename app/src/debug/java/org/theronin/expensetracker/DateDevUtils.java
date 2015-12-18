package org.theronin.expensetracker;

import org.theronin.expensetracker.utils.DateUtils;

import java.text.ParseException;
import java.util.Random;

public class DateDevUtils {

    public static long getDaysAgo(int daysAgo) {
        return System.currentTimeMillis() - (long) daysAgo * 24 * 60 * 60 * 1000;
    }

    public static String getDaysBefore(String date, int daysBefore) {
        String returnDate = null;
        try {
            long dateInMillis = DateUtils.DATE_FORMAT_FOR_STORAGE.parse(date).getTime();
            long daysBeforeInMillis = dateInMillis - (long) daysBefore * 24 * 60 * 60 * 1000;
            returnDate = DateUtils.getStorageFormattedDate(daysBeforeInMillis);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
    }

    public static long getRandomDate() {
        //Return a random date from within the last five years
        return getDaysAgo(new Random().nextInt(2000));
    }
}
