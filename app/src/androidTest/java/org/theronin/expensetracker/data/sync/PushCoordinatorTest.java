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
import org.theronin.expensetracker.model.Entry;

import java.util.List;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;

@RunWith(AndroidJUnit4.class)
public class PushCoordinatorTest {

    @Inject AbsDataSource<Entry> entryAbsDataSource;

    @Before
    public void setup() {
        TestApplication testApplication = new TestApplication();
        testApplication.inject(this);

        long count = DatabaseUtils.queryNumEntries(testApplication.getDatabase(), EntryView.VIEW_NAME);
        assertEquals("The database is not empty", 0, count);
    }

    @Test
    public void addingEntitiesSyncsThemInDatabaseWhenRemoteSyncSucceeds() throws InterruptedException {
        final int numEntries = 10;
        List<Entry> entriesToAdd = Util.createEntries(numEntries, false, SyncState.NEW);
        entryAbsDataSource.bulkInsert(entriesToAdd);

        FakeRemoteSync alwaysPassSync = new FakeRemoteSync(true);
        PushCoordinator<Entry> pushCoordinator = new PushCoordinator<>(entryAbsDataSource, alwaysPassSync);
        pushCoordinator.syncEntries(entriesToAdd);

        List<Entry> updatedEntries = entryAbsDataSource.query();
        for (Entry entry : updatedEntries) {
            assertNotSame("No global Id has been set for the entries: ", null, entry.getGlobalId());
            assertEquals("The sync state wasn't set to SYNCED", SyncState.SYNCED, entry.getSyncState());
        }
    }

    @Test
    public void addEntitiesDoesNothingWhenRemoteSyncFails() {
        int numEntries = 10;
        List<Entry> entriesToAdd = Util.createEntries(numEntries, false, SyncState.NEW);
        entryAbsDataSource.bulkInsert(entriesToAdd);

        FakeRemoteSync alwaysFailSync = new FakeRemoteSync(false);
        PushCoordinator<Entry> pushCoordinator = new PushCoordinator<>(entryAbsDataSource, alwaysFailSync);
        pushCoordinator.syncEntries(entriesToAdd);

        List<Entry> updatedEntries = entryAbsDataSource.query();
        for (Entry entry : updatedEntries) {
            assertEquals("No global IDs should be set: ", null, entry.getGlobalId());
            assertEquals("The sync state should still be NEW", SyncState.NEW, entry.getSyncState());
        }
    }

    @Test
    public void deleteSyncedEntitiesAlsoDeletesFromLocalWhenSyncSucceeds() {
        int numEntries = 10;
        List<Entry> entriesToAdd = Util.createEntries(numEntries, true, SyncState.MARKED_AS_DELETED);
        entryAbsDataSource.bulkInsert(entriesToAdd);

        FakeRemoteSync alwaysPassSync = new FakeRemoteSync(true);
        PushCoordinator<Entry> pushCoordinator = new PushCoordinator<>(entryAbsDataSource, alwaysPassSync);
        pushCoordinator.syncEntries(entriesToAdd);

        List<Entry> entriesRemaining = entryAbsDataSource.query();
        assertEquals("The remaining entries list should be empty", 0, entriesRemaining.size());
    }

    @Test
    public void deleteNonSyncedEntitiesOnlyCallsLocalDeleteWhenSyncSucceeds() throws InterruptedException {
        int numEntries = 10;
        List<Entry> entriesToAdd = Util.createEntries(numEntries, false, SyncState.MARKED_AS_DELETED);
        entryAbsDataSource.bulkInsert(entriesToAdd);

        FakeRemoteSync alwaysPassSync = new FakeRemoteSync(true);
        PushCoordinator<Entry> pushCoordinator = new PushCoordinator<>(entryAbsDataSource, alwaysPassSync);
        pushCoordinator.syncEntries(entriesToAdd);

        assertFalse("There should not have been a call to remote delete", alwaysPassSync.calledRemoteDelete());

        List<Entry> entriesRemaining = entryAbsDataSource.query();
        assertEquals("The remaining entries list should be empty", 0, entriesRemaining.size());
    }

    @Test(expected = IllegalStateException.class)
    public void syncingNewEntitiesWithGlobalIdShouldThrowException() {
        int numEntries = 10;
        List<Entry> entriesToAdd = Util.createEntries(numEntries, true, SyncState.NEW);

        FakeRemoteSync alwaysPassSync = new FakeRemoteSync(true);
        PushCoordinator<Entry> pushCoordinator = new PushCoordinator<>(entryAbsDataSource, alwaysPassSync);
        pushCoordinator.syncEntries(entriesToAdd);
    }
}
