package org.theronin.expensetracker.model;

import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.utils.DateUtils;

public class Entry extends Entity implements Comparable<Entry> {
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
                "globalId: %s, syncState: %s, date: %s, category: %s, amount: %d, currency: %s",
                globalId,
                syncState,
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Entry)) {
            return false;
        }
        Entry other = (Entry) o;

        //if the global ids don't match...
        if (globalId != null && globalId.equals(other.getGlobalId())) {
            assertContentsMatch(other);
            return true;
        }
        //then still check the local id, since we may be comparing synced with un-synced things.
        if (id > 0 && id == other.id) {
            assertContentsMatch(other);
            return true;
        }
        return false;
    }

    private void assertContentsMatch(Entry other) {
        boolean contentsMatch = amount == other.amount;

        contentsMatch = contentsMatch && isEqual(category, other.category);
        contentsMatch = contentsMatch && isEqual(currency, other.currency);

        if (!contentsMatch) {
            throw new IllegalStateException(String.format("The globalId or id of the two entries match, but their" +
                    " contents differ. (this, other)\n%s\n%s", this, other));
        }
    }

    private boolean isEqual(Entity thisPart, Entity otherPart) {
        if (thisPart == null || otherPart == null) {
            return thisPart == null && otherPart == null;
        } else {
            return thisPart.equals(otherPart);
        }
    }

    @Override
    public int compareTo(Entry another) {
        return -(int) (utcDate - another.utcDate);
    }
}
