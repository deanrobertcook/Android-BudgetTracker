package org.theronin.expensetracker.data.sync;

import android.database.DatabaseUtils;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.TestApplication;
import org.theronin.expensetracker.data.Util;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Entity;
import org.theronin.expensetracker.model.Entry;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

@RunWith(AndroidJUnit4.class)
public class EntrySaverTest {

    @Inject AbsDataSource<Entry> entryAbsDataSource;

    protected TestApplication testApplication;

    private RemoteSync alwaysPassRemoteSync = new RemoteSync() {
        @Override
        public void addEntitiesToRemote(Collection<? extends Entity> entities, Callback callback) {
            for (Entity entity : entities) {
                entity.setGlobalId(Util.generateRandomGlobalId());
                entity.setSyncState(SyncState.SYNCED);
            }
            callback.onSuccess();
        }

        @Override
        public void updateEntitiesOnRemote(Collection<? extends Entity> entities, Callback callback) {
            addEntitiesToRemote(entities, callback);
        }

        @Override
        public void deleteEntitiesFromRemote(Collection<? extends Entity> entities, Callback callback) {
            for (Entity entity : entities) {
                entity.setGlobalId(Util.generateRandomGlobalId());
                entity.setSyncState(SyncState.DELETE_SYNCED);
            }
            callback.onSuccess();
        }
    };

    @Before
    public void setup() {
        testApplication = new TestApplication();
        testApplication.inject(this);

        long count = DatabaseUtils.queryNumEntries(testApplication.getDatabase(), EntryView.VIEW_NAME);
        assertEquals("The database is not empty", 0, count);
    }

    @Test
    public void addEntriesUpdatesDatabaseWhenRemoteSyncPasses() throws InterruptedException {
        final int numEntries = 10;
        //Create some fake entries that haven't been synced yet and insert them into the database.
        List<Entry> entriesToAdd = Util.createEntries(numEntries, false, SyncState.NEW);
        entryAbsDataSource.bulkInsert(entriesToAdd);

        //Make sure they were added properly
        long count = DatabaseUtils.queryNumEntries(testApplication.getDatabase(), EntryView.VIEW_NAME);
        assertEquals("Not all entries were inserted", numEntries, count);

        //Create an entry saver with our always passing RemoteSync, and call drive it's addEntities method
        //This should force the entry source's update
        EntrySaver entrySaver = new EntrySaver(entryAbsDataSource, alwaysPassRemoteSync);
        entrySaver.addEntitiesToRemote(entriesToAdd);

        List<Entry> updatedEntries = entryAbsDataSource.query();
        assertEquals("Not all queries were available", numEntries, updatedEntries.size());

        for (Entry entry : updatedEntries) {
            assertNotSame("No global Id has been set for the entries: ", null, entry.getGlobalId());
            assertEquals("The sync state wasn't set to SYNCED", SyncState.SYNCED, entry.getSyncState());
        }
    }
}
