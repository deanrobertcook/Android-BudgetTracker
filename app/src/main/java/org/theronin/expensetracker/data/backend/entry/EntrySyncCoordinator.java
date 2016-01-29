package org.theronin.expensetracker.data.backend.entry;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.utils.DebugUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.theronin.expensetracker.data.backend.entry.SyncState.DELETE_SYNCED;
import static org.theronin.expensetracker.data.backend.entry.SyncState.NEW;

public class EntrySyncCoordinator {

    private final AbsDataSource<Entry> dataSource;
    private final EntryRemoteSync remoteSync;

    public EntrySyncCoordinator(AbsDataSource<Entry> dataSource, EntryRemoteSync remoteSync) {
        this.dataSource = dataSource;
        this.remoteSync = remoteSync;
    }

    public void syncEntries(List<Entry> entitiesToSync) {
        List<Entry> toAddOrUpdate = new ArrayList<>();
        List<Entry> toDeleteRemote = new ArrayList<>();
        List<Entry> toDeleteLocal = new ArrayList<>();

        for (Entry entity : entitiesToSync) {
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
                        entity.setSyncState(DELETE_SYNCED);
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

    private void addEntitiesToRemote(final List<Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        Timber.d("addEntities to remote");
        DebugUtils.printList(getClass().getSimpleName(), entries);
        try {
            remoteSync.saveToRemote(entries);
            for (Entry entity : entries) {
                if (entity.getSyncState() == NEW) {
                    entity.setGlobalId(remoteSync.getObjectId(entity));
                }
                entity.setSyncState(SyncState.SYNCED);
            }
            Timber.i("addEntitiesToRemote() successful, " + entries.size() + " entities synced");
            dataSource.bulkUpdate(entries);
        } catch (Exception e) {
            Timber.i("addEntitiesToRemote() failed:");
            e.printStackTrace();
        }
    }

    private void deleteEntitiesFromRemote(final List<Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        try {
            remoteSync.deleteOnRemote(entries);
            for (Entry entity : entries) {
                entity.setSyncState(DELETE_SYNCED);
            }
            Timber.i("deleteEntitiesFromRemote successful. " + entries.size() + " objects deleted");
            deleteEntitiesLocally(entries);
        } catch (Exception e) {
            Timber.i("deleteEntitiesFromRemote failed");
            e.printStackTrace();
        }
    }

    private void deleteEntitiesLocally(final List<Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        dataSource.bulkDelete(entries);
    }


    public void findEntries(long lastSync) {
        Timber.d("findEntries()");
        if (lastSync == -1) {
            remoteSync.registerForPush();
        }

        try {
            List<Entry> entries = remoteSync.pullFromRemote(lastSync);
            List<Entry> toAdd = new ArrayList<>();
            List<Entry> toDelete = new ArrayList<>();
            for (Entry entry : entries) {
                if (entry.getSyncState() == SyncState.SYNCED) {
                    toAdd.add(entry);
                } else if (entry.getSyncState() == DELETE_SYNCED) {
                    toDelete.add(entry);
                }
            }
            dataSource.bulkInsert(toAdd);
            dataSource.bulkDelete(toDelete);
        } catch (Exception e) {
            Timber.i("Pull failed");
            e.printStackTrace();
        }
    }
}
