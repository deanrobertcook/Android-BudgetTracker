package org.theronin.expensetracker.data.backend;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entity;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class PushCoordinator<T extends Entity> {

    private final AbsDataSource<T> dataSource;
    private final RemoteSync remoteSync;

    public PushCoordinator(AbsDataSource<T> dataSource, RemoteSync remoteSync) {
        this.dataSource = dataSource;
        this.remoteSync = remoteSync;
    }

    public void syncEntries(List<T> entitiesToSync) {
        List<T> toAddOrUpdate = new ArrayList<>();
        List<T> toDeleteRemote = new ArrayList<>();
        List<T> toDeleteLocal = new ArrayList<>();

        for (T entity : entitiesToSync) {
            switch (entity.getSyncState()) {
                case NEW:
                    if (entity.getGlobalId() != null) {
                        throw new IllegalStateException("New entries shouldn't have a globalId yet");
                    }
                    toAddOrUpdate.add(entity);
                    break;
                case UPDATED:
                    toAddOrUpdate.add(entity);
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

        addEntitiesToRemote(toAddOrUpdate);
        deleteEntitiesFromRemote(toDeleteRemote);
        deleteEntitiesLocally(toDeleteLocal);
    }

    private void addEntitiesToRemote(final List<T> entities) {
        if (entities.isEmpty()) {
            return;
        }
        RemoteSync.PushResult callback = new RemoteSync.PushResult() {
            @Override
            public void onSuccess() {
                Timber.i("saveEntities() successful, " + entities.size() + " entities synced");
                dataSource.bulkUpdate(entities);
            }

            @Override
            public void onFail(Exception e) {
                Timber.i("saveEntities() failed:");
                e.printStackTrace();
            }
        };
        Timber.i("Attempting to add Entities to the backend");
        remoteSync.saveEntities(entities, callback);
    }

    private void deleteEntitiesFromRemote(final List<T> entities) {
        if (entities.isEmpty()) {
            return;
        }
        RemoteSync.PushResult callback = new RemoteSync.PushResult() {
            @Override
            public void onSuccess() {
                Timber.i("deleteEntities successful. " + entities.size() + " objects deleted");
                deleteEntitiesLocally(entities);
            }

            @Override
            public void onFail(Exception e) {
                Timber.i("deleteEntities failed");
            }
        };
        Timber.i("Attempting to delete Entities from backend");
        remoteSync.deleteEntities(entities, callback);
    }

    private void deleteEntitiesLocally(final List<T> entities) {
        dataSource.bulkDelete(entities);
    }
}
