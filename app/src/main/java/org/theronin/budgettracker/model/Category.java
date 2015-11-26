package org.theronin.budgettracker.model;

import android.database.Cursor;

import org.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import org.theronin.budgettracker.utils.DateUtils;

public class Category {
    public final long id;
    public final String name;
    public final String date;
    public final long total;
    public final long frequency;

    public static final String [] projection = {
            CategoriesTable._ID,
            CategoriesTable.COL_CATEGORY_NAME,
            CategoriesTable.COL_FIRST_ENTRY_DATE,
            CategoriesTable.COL_TOTAL_AMOUNT,
            CategoriesTable.COL_ENTRY_FREQUENCY
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_CATEGORY_NAME = 1;
    public static final int INDEX_FIRST_ENTRY_DATE = 2;
    public static final int INDEX_TOTAL_AMOUNT = 3;
    public static final int INDEX_ENTRY_FREQUENCY = 3;

    public static Category fromCursor(Cursor cursor) {
        if (cursor.getColumnCount() != projection.length) {
            throw new IllegalArgumentException("The cursor supplied does not have all of the columns necessary");
        }

        return new Category(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getLong(3),
                cursor.getLong(4)
        );
    }

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
