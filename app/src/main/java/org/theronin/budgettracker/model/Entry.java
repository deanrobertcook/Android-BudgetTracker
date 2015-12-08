package org.theronin.budgettracker.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.theronin.budgettracker.data.BudgetContract.EntryTable;
import org.theronin.budgettracker.data.BudgetContract.EntryView;
import org.theronin.budgettracker.utils.DateUtils;

import static org.theronin.budgettracker.data.BudgetContract.EntryView.INDEX_AMOUNT;
import static org.theronin.budgettracker.data.BudgetContract.EntryView.INDEX_CATEGORY_ID;
import static org.theronin.budgettracker.data.BudgetContract.EntryView.INDEX_CATEGORY_NAME;
import static org.theronin.budgettracker.data.BudgetContract.EntryView.INDEX_CURRENCY_CODE;
import static org.theronin.budgettracker.data.BudgetContract.EntryView.INDEX_CURRENCY_ID;
import static org.theronin.budgettracker.data.BudgetContract.EntryView.INDEX_CURRENCY_SYMBOL;
import static org.theronin.budgettracker.data.BudgetContract.EntryView.INDEX_DATE;
import static org.theronin.budgettracker.data.BudgetContract.EntryView.INDEX_ID;

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
        long utcDateEntered = cursor.getLong(INDEX_DATE);
        long amount = cursor.getLong(INDEX_AMOUNT);

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

        if (id > -1) {
            values.put(EntryTable._ID, id);
        }

        values.put(EntryTable.COL_DATE, utcDateEntered);
        values.put(EntryTable.COL_AMOUNT, amount);

        values.put(EntryTable.COL_CATEGORY_ID, category.id);
        values.put(EntryView.COL_CATEGORY_NAME, category.name);

        values.put(EntryTable.COL_CURRENCY_ID, currency.id);
        values.put(EntryView.COL_CURRENCY_CODE, currency.code);
        return values;
    }

    @Override
    public String toString() {
        return String.format(
                "Entry: %d \n" +
                        "\t date: %s\n" +
                        "\t category: %s\n" +
                        "\t amount: %d\n" +
                        "\t currency: %s\n",
                id,
                DateUtils.getStorageFormattedDate(utcDateEntered),
                category.name,
                amount,
                currency.code
        );
    }
}
