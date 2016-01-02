package org.theronin.expensetracker.model;

import org.theronin.expensetracker.data.sync.SyncState;

public abstract class Entity {

    protected long id;
    protected String globalId;
    protected SyncState syncState;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
    public String toString() {
        return String.format("%s: id: %d, globalId: %s, syncState: %s, ",
                getClass().getSimpleName(), id, globalId, syncState);
    }
}
