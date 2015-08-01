package com.theronin.budgettracker.model;

import com.theronin.budgettracker.utils.DateUtils;

public class Category {
    public final long id;
    public final String name;
    public final String date;
    public final long total;
    public final long frequency;

    public Category(String name) {
        this(name, null);
    }

    public Category(String name, String date) {
        this(-1, name, date);
    }

    public Category(long id, String name, String date) {
        this(id, name, date, -1);
    }

    public Category(long id, String name, String date, long total) {
        this(id, name, date, total, -1);
    }

    public Category(long id, String name, String date, long total, long frequency) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.total = total;
        this.frequency = frequency;
    }

    public long getMonthlyAverage() {
        long daysPassed = DateUtils.daysSince(date);

        if (daysPassed < DateUtils.AVG_DAYS_IN_MONTH) {
            return -1;
        }

        double monthsPassed = (double) daysPassed / DateUtils.AVG_DAYS_IN_MONTH;
        return (long) ((double) total / monthsPassed);
    }
}
