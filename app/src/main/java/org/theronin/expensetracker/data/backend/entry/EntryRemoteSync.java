package org.theronin.expensetracker.data.backend.entry;

import org.theronin.expensetracker.model.Entry;

import java.util.List;

public interface EntryRemoteSync {

    void saveToRemote(List<Entry> entries) throws Exception;

    String getObjectId(Entry entry);

    void deleteOnRemote(List<Entry> entries) throws Exception;

    abstract void registerForPush();

    abstract List<Entry> pullFromRemote(long lastSync) throws Exception;

}
