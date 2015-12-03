package org.theronin.budgettracker.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.theronin.budgettracker.data.BudgetContract.EntriesTable;
import org.theronin.budgettracker.data.BudgetContract.EntriesView;

import static org.theronin.budgettracker.data.BudgetContract.EntriesView.INDEX_AMOUNT_CENTS;
import static org.theronin.budgettracker.data.BudgetContract.EntriesView.INDEX_CATEGORY_ID;
import static org.theronin.budgettracker.data.BudgetContract.EntriesView.INDEX_CATEGORY_NAME;
import static org.theronin.budgettracker.data.BudgetContract.EntriesView.INDEX_CURRENCY_CODE;
import static org.theronin.budgettracker.data.BudgetContract.EntriesView.INDEX_CURRENCY_ID;
import static org.theronin.budgettracker.data.BudgetContract.EntriesView.INDEX_CURRENCY_SYMBOL;
import static org.theronin.budgettracker.data.BudgetContract.EntriesView.INDEX_DATE_ENTERED;
import static org.theronin.budgettracker.data.BudgetContract.EntriesView.INDEX_ID;

public class Entry {
    public final long id;
    public final long utcDateEntered;
    public final long amount;
    public final Category category;
    public final Currency currency;

    public Entry(
            long utcDateEntered,
            long amount,
            Category category,
            Currency currency) {
        this(-1, utcDateEntered, amount, category, currency);
    }

    public Entry(
            long id,
            long utcDateEntered,
            long amount,
            Category category,
            Currency currency) {
        this.id = id;
        this.utcDateEntered = utcDateEntered;
        this.amount = amount;
        this.category = category;
        this.currency = currency;
    }

    public static Entry fromCursor(Cursor cursor) {
        long id = cursor.getLong(INDEX_ID);
        long utcDateEntered = cursor.getLong(INDEX_DATE_ENTERED);
        long amount = cursor.getLong(INDEX_AMOUNT_CENTS);

        Category category = new Category(
                cursor.getLong(INDEX_CATEGORY_ID),
                cursor.getString(INDEX_CATEGORY_NAME)
        );

        Currency currency = new Currency(
                cursor.getLong(INDEX_CURRENCY_ID),
                cursor.getString(INDEX_CURRENCY_CODE),
                cursor.getString(INDEX_CURRENCY_SYMBOL)
        );

        return new Entry(id, utcDateEntered, amount, category, currency);
    }

    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(EntriesTable.COL_DATE_ENTERED, utcDateEntered);
        values.put(EntriesTable.COL_AMOUNT_CENTS, amount);

        values.put(EntriesTable.COL_CATEGORY_ID, category.id);
        values.put(EntriesView.COL_CATEGORY_NAME, category.name);

        values.put(EntriesTable.COL_CURRENCY_ID, currency.id);
        values.put(EntriesView.COL_CURRENCY_CODE, currency.code);
        return values;
    }
}
