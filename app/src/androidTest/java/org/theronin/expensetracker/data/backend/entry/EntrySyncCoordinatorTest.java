package org.theronin.expensetracker.data.backend.entry;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.model.Entry;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.theronin.expensetracker.testutils.MockitoMatchers.containsAllEntries;

@RunWith(AndroidJUnit4.class)
public class EntrySyncCoordinatorTest {

    private AbsDataSource<Entry> entryAbsDataSource;
    private EntryRemoteSync remoteSync;
    private EntrySyncCoordinator entrySyncCoordinator;

    @Before
    public void setup() {
        entryAbsDataSource = mock(DataSourceEntry.class);
        remoteSync = mock(EntryRemoteSync.class);
        entrySyncCoordinator = new EntrySyncCoordinator(entryAbsDataSource, remoteSync);
    }

    @Test
    public void addingEntitiesSyncsThemInDatabaseWhenRemoteSyncSucceeds() throws InterruptedException {
        List<Entry> entriesToAdd = Arrays.asList(
                new Entry(1, null, SyncState.NEW, -1, -1, null, null)
        );

        when(remoteSync.getObjectId(entriesToAdd.get(0))).thenReturn("xyz");

        entrySyncCoordinator.syncEntries(entriesToAdd);

        List<Entry> expectedEntriesToSave = Arrays.asList(
                new Entry(1, "xyz", SyncState.SYNCED, -1, -1, null, null)
        );

        verify(entryAbsDataSource).bulkUpdate(containsAllEntries(expectedEntriesToSave));
    }

    @Test
    public void addEntitiesDoesNothingWhenRemoteSyncFails() throws Exception {
        List<Entry> entriesToAdd = Arrays.asList(
                new Entry(1, null, SyncState.NEW, -1, -1, null, null)
        );

        doThrow(new Exception("Failed download")).when(remoteSync).saveToRemote(entriesToAdd);

        entrySyncCoordinator.syncEntries(entriesToAdd);

        verifyNoMoreInteractions(entryAbsDataSource);
    }

    @Test
    public void deleteSyncedEntitiesAlsoDeletesFromLocalWhenSyncSucceeds() {
        List<Entry> entriesToAdd = Arrays.asList(
                new Entry(1, "xyz", SyncState.MARKED_AS_DELETED, -1, -1, null, null)
        );

        entrySyncCoordinator.syncEntries(entriesToAdd);

        List<Entry> expectedEntriesToBeDeleted = Arrays.asList(
                new Entry(1, "xyz", SyncState.DELETE_SYNCED, -1, -1, null, null)
        );

        verify(entryAbsDataSource).bulkDelete(containsAllEntries(expectedEntriesToBeDeleted));
    }

    @Test
    public void deleteNonSyncedEntitiesOnlyCallsLocalDeleteWhenSyncSucceeds() throws InterruptedException {
        List<Entry> entriesToAdd = Arrays.asList(
                new Entry(1, null, SyncState.MARKED_AS_DELETED, -1, -1, null, null)
        );

        entrySyncCoordinator.syncEntries(entriesToAdd);

        List<Entry> expectedEntriesToBeDeleted = Arrays.asList(
                new Entry(1, null, SyncState.DELETE_SYNCED, -1, -1, null, null)
        );

        verify(entryAbsDataSource).bulkDelete(containsAllEntries(expectedEntriesToBeDeleted));
        verifyNoMoreInteractions(remoteSync);
    }

    @Test(expected = IllegalStateException.class)
    public void syncingNewEntitiesWithGlobalIdShouldThrowException() {
        List<Entry> entriesToAdd = Arrays.asList(
                new Entry(1, "xyz", SyncState.NEW, -1, -1, null, null)
        );

        entrySyncCoordinator.syncEntries(entriesToAdd);

        verifyNoMoreInteractions(entryAbsDataSource);
    }
}
