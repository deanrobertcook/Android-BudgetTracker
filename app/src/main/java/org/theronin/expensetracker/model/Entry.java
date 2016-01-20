package org.theronin.expensetracker.model;

import org.theronin.expensetracker.data.backend.SyncState;
import org.theronin.expensetracker.utils.DateUtils;

public class Entry extends Entity{
    public final long utcDate;
    public final long amount;
    public final Category category;
    public final Currency currency;

    private String globalId;
    private SyncState syncState;
    private long homeAmount = -1;

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

    @Override
    public String toString() {
        return super.toString() + String.format(
                "date: %s, category: %s, amount: %d, currency: %s",
                DateUtils.getStorageFormattedDate(utcDate),
                category == null ? null : category.name,
                amount,
                currency == null ? null : currency.code
        );
    }

    public void setHomeAmount(long amount) {
        this.homeAmount = amount;
    }

    public long getHomeAmount() {
        return homeAmount;
    }

    public boolean hasGlobalId() {
        return globalId != null && globalId.length() > 0;
    }

    public String getGlobalId() {
        return globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    public SyncState getSyncState() {
        return syncState;
    }

    public void setSyncState(SyncState syncState) {
        this.syncState = syncState;
    }
}
