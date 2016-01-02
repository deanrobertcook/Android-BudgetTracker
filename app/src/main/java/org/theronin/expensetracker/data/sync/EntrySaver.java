package org.theronin.expensetracker.data.sync;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entry;

import java.util.List;

import timber.log.Timber;

public class EntrySaver implements EntitySaver<Entry> {

    private final AbsDataSource<Entry> entryAbsDataSource;
    private final RemoteSync remoteSync;

    public EntrySaver(AbsDataSource<Entry> entryAbsDataSource,
                      RemoteSync remoteSync) {
        this.entryAbsDataSource = entryAbsDataSource;
        this.remoteSync = remoteSync;
    }

    @Override
    public void addEntitiesToRemote(final List<Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        RemoteSync.Callback callback = new RemoteSync.Callback() {
            @Override
            public void onSuccess() {
                Timber.i("saveEntities() successful, " + entries.size() + " entities synced");
                entryAbsDataSource.bulkUpdate(entries);
            }

            @Override
            public void onFail(Exception e) {
                Timber.i("saveEntities() failed:");
                e.printStackTrace();
            }
        };
        remoteSync.saveEntities(entries, callback);
    }

    @Override
    public void updateEntitiesOnRemote(final List<Entry> entries) {
        addEntitiesToRemote(entries);
    }

    @Override
    public void deleteEntitiesFromRemote(final List<Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        RemoteSync.Callback callback = new RemoteSync.Callback() {
            @Override
            public void onSuccess() {
                Timber.i("deleteEntities successful. " + entries.size() + " objects deleted");
                entryAbsDataSource.bulkDelete(entries);
            }

            @Override
            public void onFail(Exception e) {
                Timber.i("deleteEntities failed");
            }
        };
        remoteSync.deleteEntities(entries, callback);
    }

    @Override
    public void deleteEntitiesLocally(final List<Entry> entries) {
        entryAbsDataSource.bulkDelete(entries);
    }
}
