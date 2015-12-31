package org.theronin.expensetracker.data.sync;

import org.theronin.expensetracker.model.Entity;

import java.util.Collection;

public interface RemoteSync {

    interface Callback {
        void onSuccess();
        void onFail(Exception e);
    }

    void addEntitiesToRemote(Collection<? extends Entity> entities, Callback callback);
    void updateEntitiesOnRemote(Collection<? extends Entity> entities, Callback callback);
    void deleteEntitiesFromRemote(Collection<? extends Entity> entities, Callback callback);
}
