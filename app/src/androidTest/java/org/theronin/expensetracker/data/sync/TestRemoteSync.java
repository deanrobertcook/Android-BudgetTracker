package org.theronin.expensetracker.data.sync;

import org.theronin.expensetracker.data.Util;
import org.theronin.expensetracker.model.Entity;

import java.util.Collection;

public class TestRemoteSync implements RemoteSync {

    private boolean alwaysPass;

    public TestRemoteSync(boolean alwaysPass) {
        this.alwaysPass = alwaysPass;
    }

    @Override
    public void addEntitiesToRemote(Collection<? extends Entity> entities, Callback callback) {
        if (alwaysPass) {
            for (Entity entity : entities) {
                entity.setGlobalId(Util.generateRandomGlobalId());
                entity.setSyncState(SyncState.SYNCED);
            }
            callback.onSuccess();
        } else {
            callback.onFail(new RuntimeException("Test remote sync failed"));
        }

    }

    @Override
    public void updateEntitiesOnRemote(Collection<? extends Entity> entities, Callback callback) {
        addEntitiesToRemote(entities, callback);
    }

    @Override
    public void deleteEntitiesFromRemote(Collection<? extends Entity> entities, Callback callback) {
        if (alwaysPass) {
            for (Entity entity : entities) {
                entity.setGlobalId(Util.generateRandomGlobalId());
                entity.setSyncState(SyncState.DELETE_SYNCED);
            }
            callback.onSuccess();
        } else {
            callback.onFail(new RuntimeException("Test remote sync failed"));
        }
    }
}
