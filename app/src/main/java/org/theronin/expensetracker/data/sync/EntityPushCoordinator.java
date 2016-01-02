package org.theronin.expensetracker.data.sync;

import org.theronin.expensetracker.model.Entity;

import java.util.ArrayList;
import java.util.List;

public class EntityPushCoordinator<T extends Entity> {

    private EntitySaver<T> entitySaver;

    interface EntitySaver<U extends Entity> {
        void addEntitiesToRemote(List<U> entries);
        void updateEntitiesOnRemote(List<U> entries);
        void deleteEntitiesFromRemote(List<U> entries);
        void deleteEntitiesLocally(List<U> entries);
    }

    public EntityPushCoordinator(EntitySaver<T> entitySaver) {
        this.entitySaver = entitySaver;
    }

    public void syncEntries(List<T> entitiesToSync) {
        List<T> toAdd = new ArrayList<>();
        List<T> toUpdate = new ArrayList<>();
        List<T> toDeleteRemote = new ArrayList<>();
        List<T> toDeleteLocal = new ArrayList<>();

        for (T entity : entitiesToSync) {
            switch (entity.getSyncState()) {
                case NEW:
                    if (entity.getGlobalId() != null) {
                        throw new IllegalStateException("New entries shouldn't have a globalId yet");
                    }
                    toAdd.add(entity);
                    break;
                case UPDATED:
                    toUpdate.add(entity);
                    break;
                case MARKED_AS_DELETED:
                    if (entity.getGlobalId() == null) {
                        entity.setSyncState(SyncState.DELETE_SYNCED);
                        toDeleteLocal.add(entity);
                    } else {
                        toDeleteRemote.add(entity);
                    }
                    break;
                case DELETE_SYNCED:
                    toDeleteLocal.add(entity);
                    break;
            }
        }

        entitySaver.addEntitiesToRemote(toAdd);
        entitySaver.updateEntitiesOnRemote(toUpdate);
        entitySaver.deleteEntitiesFromRemote(toDeleteRemote);
        entitySaver.deleteEntitiesLocally(toDeleteLocal);
    }
}
