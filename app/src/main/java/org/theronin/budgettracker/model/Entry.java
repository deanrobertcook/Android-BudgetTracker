package org.theronin.budgettracker.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.theronin.budgettracker.data.BudgetContract.CategoriesView;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;

public class Entry {
    public final long id;
    public final String categoryName;
    public final long utcDateEntered;
    public final String currencyEntered;
    public final long amount;

    public static final String [] projection = {
            EntriesTable.TABLE_NAME + "." + EntriesTable._ID,
            CategoriesView.VIEW_NAME + "." + CategoriesView.COL_CATEGORY_NAME,
            EntriesTable.COL_DATE_ENTERED,
            EntriesTable.COL_CURRENCY_ENTERED,
            EntriesTable.COL_AMOUNT_CENTS
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_CATEGORY_NAME = 1;
    public static final int INDEX_DATE_ENTERED = 2;
    public static final int INDEX_CURRENCY_ENTERED = 3;
    public static final int INDEX_AMOUNT_CENTS = 4;

    public Entry(String categoryName, long utcDateEntered, String currencyEntered, long amount) {
        this(-1, categoryName, utcDateEntered, currencyEntered, amount);
    }

    public Entry (long id, String categoryName, long utcDateEntered, String currencyEntered, long amount) {
        this.id = id;
        this.categoryName = categoryName;
        this.utcDateEntered = utcDateEntered;
        this.amount = amount;
        this.currencyEntered = currencyEntered;
    }


    public static Entry fromCursor(Cursor cursor) {
        return new Entry(
                cursor.getLong(INDEX_ID),
                cursor.getString(INDEX_CATEGORY_NAME),
                cursor.getLong(INDEX_DATE_ENTERED),
                cursor.getString(INDEX_CURRENCY_ENTERED),
                cursor.getLong(INDEX_AMOUNT_CENTS)
        );
    }

    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(EntriesTable._ID, id);
        values.put(projection[INDEX_CATEGORY_NAME], categoryName);
        values.put(projection[INDEX_DATE_ENTERED], utcDateEntered);
        values.put(projection[INDEX_AMOUNT_CENTS], amount);
        values.put(projection[INDEX_CURRENCY_ENTERED], currencyEntered);
        return values;
    }
}
