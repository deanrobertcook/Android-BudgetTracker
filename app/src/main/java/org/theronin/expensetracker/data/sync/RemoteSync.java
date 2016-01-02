package org.theronin.expensetracker.data.sync;

import org.theronin.expensetracker.model.Entity;

import java.util.List;

/**
 * This class abstracts away the client side handling of Entities that are synced with the backend.
 * Such behaviour includes setting the sync states and globalIds of those Entities, all of which
 * is not important to the actual implementation in how the data gets to the backend. This should
 * hopefully make it easier if I decide to use a different backend, since the syncing logic should
 * remain relatively unchanged.
 */
public abstract class RemoteSync {

    interface Callback {
        void onSuccess();
        void onFail(Exception e);
    }

    public void saveEntities(List<? extends Entity> entities, Callback callback) {
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

    public void deleteEntities(List<? extends Entity> entities, Callback callback) {
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
}
