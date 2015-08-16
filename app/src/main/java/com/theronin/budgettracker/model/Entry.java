package com.theronin.budgettracker.model;

import android.database.Cursor;

import com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import com.theronin.budgettracker.data.BudgetContract.EntriesTable;

public class Entry {
    public final long id;
    public final String categoryName;
    public final String dateEntered;
    public final long amount;

    public static final String [] projection = {
            EntriesTable.TABLE_NAME + "." + EntriesTable._ID,
            CategoriesTable.TABLE_NAME + "." + CategoriesTable.COL_CATEGORY_NAME,
            EntriesTable.COL_DATE_ENTERED,
            EntriesTable.COL_AMOUNT_CENTS
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_CATEGORY_NAME = 1;
    public static final int INDEX_DATE_ENTERED = 2;
    public static final int INDEX_AMOUNT_CENTS = 3;

    public Entry(String categoryName, String dateEntered, long amount) {
        this(-1, categoryName, dateEntered, amount);
    }

    public Entry (long id, String categoryName, String dateEntered, long amount) {
        this.id = id;
        this.categoryName = categoryName;
        this.dateEntered = dateEntered;
        this.amount = amount;
    }


    public static Entry fromCursor(Cursor cursor) {
        return new Entry(
                cursor.getLong(INDEX_ID),
                cursor.getString(INDEX_CATEGORY_NAME),
                cursor.getString(INDEX_DATE_ENTERED),
                cursor.getLong(INDEX_AMOUNT_CENTS)
        );
    }


}
