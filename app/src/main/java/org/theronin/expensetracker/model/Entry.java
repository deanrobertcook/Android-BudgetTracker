package org.theronin.expensetracker.model;

import com.parse.ParseObject;

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
        SyncState syncState = object.getBoolean("isDeleted") ? SyncState.DELETE_SYNCED : SyncState.SYNCED;
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
    public String toString() {
        return super.toString() + String.format(
                "date: %s, category: %s, amount: %d, currency: %s",
                DateUtils.getStorageFormattedDate(utcDate), category.name, amount, currency.code
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
