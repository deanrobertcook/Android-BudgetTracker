package org.theronin.expensetracker.model;

import org.apache.commons.lang.WordUtils;
import org.theronin.expensetracker.utils.DateUtils;

public class Category extends Entity {

    private String name;

    public final long utcFirstEntryDate;
    public final long frequency;
    private long total = -1;

    private int missingEntries = 1;
    public Category(String name) {
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
        setName(name);
        this.utcFirstEntryDate = utcFirstEntryDate;
        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return WordUtils.capitalize(getName());
    }

    public String getDisplayNameWithFrequency() {
        return String.format("%s (%d)", getDisplayName(), frequency);
    }

    public void setName(String name) {
        this.name = sanitiseName(name);
    }

    private String sanitiseName(String categoryName) {
        categoryName = categoryName.toLowerCase();
        categoryName = categoryName.trim();
        return categoryName;
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
