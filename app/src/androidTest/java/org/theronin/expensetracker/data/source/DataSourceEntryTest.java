package org.theronin.expensetracker.data.source;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.theronin.expensetracker.data.Util;
import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.testutils.InMemoryDataSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static org.theronin.expensetracker.testutils.Constants.LATCH_WAIT;

@RunWith(AndroidJUnit4.class)
public class DataSourceEntryTest {

    AbsDataSource<Entry> entryAbsDataSource;

    @Before
    public void setup() {
        entryAbsDataSource = new InMemoryDataSource().getEntryDataSource();
    }

    @Test @SmallTest
    public void observersAreNotifiedOnDataDeletion() throws InterruptedException {
        final CountDownLatch callbackLatch = new CountDownLatch(1);

        Entry insertedEntry = entryAbsDataSource.insert(new Entry(null, SyncState.DELETE_SYNCED, 100L, 100L, new Category("Test"), new Currency("AUD")));
        AbsDataSource.Observer observer = new AbsDataSource.Observer() {
            @Override
            public void onDataSourceChanged() {
                callbackLatch.countDown();
            }
        };

        entryAbsDataSource.registerObserver(observer);
        entryAbsDataSource.delete(insertedEntry);

        callbackLatch.await(LATCH_WAIT, TimeUnit.MILLISECONDS);
        assertEquals("Observer was not notified", 0, callbackLatch.getCount());
    }

    @Test @SmallTest
    public void testBulkUpdatesArePerformed() throws InterruptedException {
        List<Entry> entries = Util.createEntries(10, true, SyncState.SYNCED);
        entryAbsDataSource.bulkInsert(entries);

        for (Entry entry : entries) {
            entry.setSyncState(SyncState.UPDATED);
        }
        entryAbsDataSource.bulkUpdate(entries);

        List<Entry> updatedEntries = entryAbsDataSource.query();
        for (Entry entry : updatedEntries) {
            assertEquals("The sync state should have been changed", SyncState.UPDATED, entry.getSyncState());
        }
    }

    @Test @SmallTest
    public void addingEntriesWithMatchingGlobalIdsDoesNothing() {
        List<Entry> entriesWithIds = Arrays.asList(
                new Entry(1, "abc", SyncState.UPDATED, -1, -1, new Category("Test"), new Currency("AUD")),
                new Entry(2, "bcd", SyncState.UPDATED, -1, -1, new Category("Test"), new Currency("AUD")),
                new Entry(3, "cde", SyncState.UPDATED, -1, -1, new Category("Test"), new Currency("AUD"))
        );

        List<Entry> sameWithoutLocalIds = Arrays.asList(
                new Entry(-1, "abc", SyncState.UPDATED, -1, -1, new Category("Test"), new Currency("AUD")),
                new Entry(-1, "bcd", SyncState.UPDATED, -1, -1, new Category("Test"), new Currency("AUD")),
                new Entry(-1, "cde", SyncState.UPDATED, -1, -1, new Category("Test"), new Currency("AUD"))
        );

        entryAbsDataSource.bulkInsert(entriesWithIds);

        assertEquals(3, entryAbsDataSource.query().size());

        entryAbsDataSource.bulkInsert(sameWithoutLocalIds);

        assertEquals(3, entryAbsDataSource.query().size());

    }
}