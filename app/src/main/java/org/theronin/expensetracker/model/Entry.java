package org.theronin.expensetracker.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.theronin.expensetracker.data.Contract.EntryTable;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.utils.DateUtils;

import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_AMOUNT;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CATEGORY_ID;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CATEGORY_NAME;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CURRENCY_CODE;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CURRENCY_ID;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CURRENCY_SYMBOL;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_DATE;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_GLOBAL_ID;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_ID;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_TO_SYNC;

public class Entry {
    public final long id;
    public final String globalId;
    public final boolean toSync;
    public final long utcDate;
    public final long amount;
    public final Category category;
    public final Currency currency;

    private double directExchangeRate = -1;

    public Entry(
            long utcDate,
            long amount,
            Category category,
            Currency currency) {
        this(-1, null, false, utcDate, amount, category, currency);
    }

    public Entry(
            String globalId,
            long utcDate,
            long amount,
            Category category,
            Currency currency) {
        this(-1, globalId, false, utcDate, amount, category, currency);
    }

    public Entry(
            long id,
            String globalId,
            boolean toSync,
            long utcDate,
            long amount,
            Category category,
            Currency currency) {
        this.id = id;
        this.globalId = globalId;
        this.toSync = toSync;
        this.utcDate = utcDate;
        this.amount = amount;
        this.category = category;
        this.currency = currency;
    }

    public static Entry fromCursor(Cursor cursor) {
        long id = cursor.getLong(INDEX_ID);
        String globalId = cursor.getString(INDEX_GLOBAL_ID);
        boolean toSync = cursor.getInt(INDEX_TO_SYNC) == 1;
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

        return new Entry(id, globalId, toSync, utcDateEntered, amount, category, currency);
    }

    public ContentValues toValues() {
        ContentValues values = new ContentValues();

        if (id > -1) {
            values.put(EntryTable._ID, id);
        }

        values.put(EntryTable.COL_DATE, utcDate);
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
                "Entry: %d,  date: %s, category: %s, amount: %d, currency: %s",
                id,
                DateUtils.getStorageFormattedDate(utcDate),
                category.name,
                amount,
                currency.code
        );
    }

    public double getDirectExchangeRate() {
        return directExchangeRate;
    }

    /**
     * Note, unlike everywhere else in the application, this will be the direct exchange rate
     * @param directExchangeRate
     */
    public void setDirectExchangeRate(double directExchangeRate) {
        this.directExchangeRate = directExchangeRate;
    }
}
