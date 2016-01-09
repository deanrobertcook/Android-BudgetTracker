package org.theronin.expensetracker.data.backend;

public enum SyncState {
    NEW,
    UPDATED,
    SYNCED,
    MARKED_AS_DELETED,
    DELETE_SYNCED;

    public static String deleteStateSelection() {
        return "'" + MARKED_AS_DELETED + "', '" + DELETE_SYNCED + "'";
    }
}
