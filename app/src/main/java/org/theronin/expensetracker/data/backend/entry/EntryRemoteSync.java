package org.theronin.expensetracker.data.backend.entry;

import org.theronin.expensetracker.model.Entity;
import org.theronin.expensetracker.model.Entry;

import java.util.List;

public interface EntryRemoteSync {

    void saveToRemote(List<? extends Entity> entities) throws Exception;

    String getObjectId(Entity entity);

    void deleteOnRemote(List<? extends Entity> entities) throws Exception;

    abstract void registerForPush();

    abstract List<Entry> pullFromRemote(long lastSync) throws Exception;

}
