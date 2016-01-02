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
import static junit.framework.Assert.assertNotSame;

@RunWith(AndroidJUnit4.class)
public class EntrySaverTest {

    @Inject AbsDataSource<Entry> entryAbsDataSource;

    @Before
    public void setup() {
        TestApplication testApplication = new TestApplication();
        testApplication.inject(this);

        long count = DatabaseUtils.queryNumEntries(testApplication.getDatabase(), EntryView.VIEW_NAME);
        assertEquals("The database is not empty", 0, count);
    }

    @Test
    public void addEntriesUpdatesDatabaseWhenRemoteSyncPasses() throws InterruptedException {
        final int numEntries = 10;
        List<Entry> entriesToAdd = Util.createEntries(numEntries, false, SyncState.NEW);
        entryAbsDataSource.bulkInsert(entriesToAdd);

        RemoteSync alwaysPassSync = new FakeRemoteSync(true);
        EntrySaver entrySaver = new EntrySaver(entryAbsDataSource, alwaysPassSync);
        entrySaver.addEntitiesToRemote(entriesToAdd);

        List<Entry> updatedEntries = entryAbsDataSource.query();
        for (Entry entry : updatedEntries) {
            assertNotSame("No global Id has been set for the entries: ", null, entry.getGlobalId());
            assertEquals("The sync state wasn't set to SYNCED", SyncState.SYNCED, entry.getSyncState());
        }
    }

    @Test
    public void addEntriesDoesNothingWhenRemoteSyncFails() {
        int numEntries = 10;
        List<Entry> entriesToAdd = Util.createEntries(numEntries, false, SyncState.NEW);
        entryAbsDataSource.bulkInsert(entriesToAdd);

        RemoteSync alwaysFailSync = new FakeRemoteSync(false);
        EntrySaver entrySaver = new EntrySaver(entryAbsDataSource, alwaysFailSync);
        entrySaver.addEntitiesToRemote(entriesToAdd);

        List<Entry> updatedEntries = entryAbsDataSource.query();
        for (Entry entry : updatedEntries) {
            assertEquals("No global IDs should be set: ", null, entry.getGlobalId());
            assertEquals("The sync state should still be NEW", SyncState.NEW, entry.getSyncState());
        }
    }
}
