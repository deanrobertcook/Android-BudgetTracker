package org.theronin.expensetracker.model;

import org.theronin.expensetracker.utils.DateUtils;

public class Category extends Entity {
    public final String name;
    public final long utcFirstEntryDate;
    public final long frequency;

    private long total = -1;
    private int missingEntries = 1;

    public Category(String name) {
        //TODO check what a good default date is.
        this(name, 0);
    }

    public Category(long id, String name) {
        this(id, name, 0);
    }

    public Category(String name, long utcFirstEntryDate) {
        this(-1, name, utcFirstEntryDate);
    }

    public Category(long id, String name, long utcFirstEntryDate) {
        this(id, name, utcFirstEntryDate, -1);
    }

    public Category(long id, String name, long utcFirstEntryDate, long frequency) {
        this.id = id;
        this.name = name;
        this.utcFirstEntryDate = utcFirstEntryDate;
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(
                "name: %s, first entered: %s, entry frequency: %d",
                name, utcFirstEntryDate, frequency
        );
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getMonthlyAverage() {
        if (total == -1) {
            return -1;
        }
        long daysPassed = DateUtils.daysSince(utcFirstEntryDate);

        if (daysPassed < DateUtils.AVG_DAYS_IN_MONTH) {
            return -1;
        }

        double monthsPassed = (double) daysPassed / DateUtils.AVG_DAYS_IN_MONTH;
        return (long) ((double) total / monthsPassed);
    }

    public int getMissingEntries() {
        return missingEntries;
    }

    public void setMissingEntries(int missingEntries) {
        this.missingEntries = missingEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Category)) {
            return false;
        }
        return name.equals(((Category) o).name);
    }
}
