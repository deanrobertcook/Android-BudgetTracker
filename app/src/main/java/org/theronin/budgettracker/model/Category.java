package org.theronin.budgettracker.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.theronin.budgettracker.utils.DateUtils;

import static org.theronin.budgettracker.data.BudgetContract.CategoryView.COL_CATEGORY_NAME;
import static org.theronin.budgettracker.data.BudgetContract.CategoryView.INDEX_CATEGORY_NAME;
import static org.theronin.budgettracker.data.BudgetContract.CategoryView.INDEX_ENTRY_FREQUENCY;
import static org.theronin.budgettracker.data.BudgetContract.CategoryView.INDEX_FIRST_ENTRY_DATE;
import static org.theronin.budgettracker.data.BudgetContract.CategoryView.INDEX_ID;
import static org.theronin.budgettracker.data.BudgetContract.CategoryView.PROJECTION;

public class Category {
    private static final String TAG = Category.class.getName();
    public final long id;
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

    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(COL_CATEGORY_NAME, name);
        return values;
    }

    public static Category fromCursor(Cursor cursor) {
        if (cursor.getColumnCount() != PROJECTION.length) {
            throw new IllegalArgumentException("The cursor supplied does not have all of the columns necessary");
        }
        long id = cursor.getLong(INDEX_ID);
        String categoryName = cursor.getString(INDEX_CATEGORY_NAME);
        long utcDateFirstEntered = cursor.getLong(INDEX_FIRST_ENTRY_DATE);
        long entryFrequency = cursor.getLong(INDEX_ENTRY_FREQUENCY);

        return new Category(id, categoryName, utcDateFirstEntered, entryFrequency);
    }

    @Override
    public String toString() {
        return String.format(
                "Category %s\n" +
                        "\t id: %d\n" +
                        "\t first entered: %s\n" +
                        "\t entry frequency: %d\n",
                name, id, utcFirstEntryDate, frequency
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
}
