package org.theronin.expensetracker.data.sync;

import org.theronin.expensetracker.model.Entity;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * This class abstracts away the client side handling of Entities that are synced with the backend.
 * Such behaviour includes setting the sync states and globalIds of those Entities, all of which
 * is not important to the actual implementation in how the data gets to the backend. This should
 * hopefully make it easier if I decide to use a different backend, since the syncing logic should
 * remain relatively unchanged.
 */
public abstract class RemoteSync {

    interface PushResult {
        void onSuccess();
        void onFail(Exception e);
    }

    interface PullResult {
        void addEntries(List<Entry> entries);
        void deleteEntries(List<Entry> entries);
        void onComplete();
        void onFail(Exception e);
    }

    public void saveEntities(List<? extends Entity> entities, PushResult callback) {
        try {
            bulkAddOperation(entities);
            for (Entity entity : entities) {
                if (entity.getSyncState() == SyncState.NEW) {
                    entity.setGlobalId(getObjectId(entity));
                }
                entity.setSyncState(SyncState.SYNCED);
            }
            callback.onSuccess();
        } catch (Exception e) {
            callback.onFail(e);
        }
    }

    protected abstract void bulkAddOperation(List<? extends Entity> entities) throws Exception;

    protected abstract String getObjectId(Entity entity);

    public void deleteEntities(List<? extends Entity> entities, PushResult callback) {
        try {
            bulkDeleteOperation(entities);
            for (Entity entity : entities) {
                entity.setSyncState(SyncState.DELETE_SYNCED);
            }
            callback.onSuccess();
        } catch (Exception e) {
            callback.onFail(e);
        }
    }

    protected abstract void bulkDeleteOperation(List<? extends Entity> entities) throws Exception;

    public void findEntries(long lastSync, PullResult callback) {

        if (lastSync == -1) {
            registerForPush();
        }

        try {
            List<Entry> entries = findOperation(lastSync);
            List<Entry> toAdd = new ArrayList<>();
            List<Entry> toDelete = new ArrayList<>();
            for (Entry entry : entries) {
                if (entry.getSyncState() == SyncState.SYNCED) {
                    toAdd.add(entry);
                } else if (entry.getSyncState() == SyncState.DELETE_SYNCED) {
                    toDelete.add(entry);
                }
            }
            callback.addEntries(toAdd);
            callback.deleteEntries(toDelete);
            callback.onComplete();
        } catch (Exception e) {
            callback.onFail(e);
        }
    }

    protected abstract void registerForPush();
    protected abstract List<Entry> findOperation(long lastSync) throws Exception;

}
