package org.theronin.expensetracker.model;

import android.content.ContentValues;

import com.parse.ParseObject;

import org.theronin.expensetracker.data.Contract.EntryTable;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.sync.SyncState;
import org.theronin.expensetracker.utils.DateUtils;

public class Entry extends Entity{
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
        this(-1, null, SyncState.NEW, utcDate, amount, category, currency);
    }

    public Entry(
            String globalId,
            long utcDate,
            long amount,
            Category category,
            Currency currency) {
        //TODO This is used when reading files from backup agent - they may already exist
        //TODO on the backend, so this could be a tricky corner case. Consider deleting
        //TODO file backup agent
        this(-1, globalId, SyncState.UPDATED, utcDate, amount, category, currency);
    }

    public Entry(
            String globalId,
            SyncState syncState,
            long utcDate,
            long amount,
            Category category,
            Currency currency) {
        this(-1, globalId, syncState, utcDate, amount, category, currency);
    }


    public Entry(
            long id,
            String globalId,
            SyncState syncState,
            long utcDate,
            long amount,
            Category category,
            Currency currency) {
        this.id = id;
        this.globalId = globalId;
        this.syncState = syncState;
        this.utcDate = utcDate;
        this.amount = amount;
        this.category = category;
        this.currency = currency;
    }

    public static Entry fromParseObject(ParseObject object) {
        String globalId = object.getObjectId();
        SyncState syncState = SyncState.SYNCED;
        long utcDate = object.getLong("date");
        long amount = object.getLong("amount");

        Category category = new Category(
                object.getString("category")
        );

        Currency currency = new Currency(
                object.getString("currency")
        );

        return new Entry(globalId, syncState, utcDate, amount, category, currency);
    }

    @Override
    public ParseObject toParseObject() {
        ParseObject object;
        if (hasGlobalId()) {
            object = ParseObject.createWithoutData(EntryView.VIEW_NAME, globalId);
        } else {
            object = new ParseObject(EntryView.VIEW_NAME);
        }
        object.put("amount", this.amount);
        object.put("category", this.category.name);
        object.put("currency", this.currency.code);
        object.put("date", this.utcDate);
        return object;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();

        if (id > -1) {
            values.put(EntryTable._ID, id);
        }

        values.put(EntryTable.COL_GLOBAL_ID, globalId);
        values.put(EntryTable.COL_SYNC_STATUS, syncState.name());

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
                "Entry: %s,  date: %s, category: %s, amount: %d, currency: %s",
                globalId,
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
