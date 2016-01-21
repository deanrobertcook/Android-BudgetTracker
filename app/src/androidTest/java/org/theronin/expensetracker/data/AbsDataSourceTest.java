package org.theronin.expensetracker.data;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AbsDataSourceTest {

    private final int DEFAULT_LATCH_WAIT = 2000;

    private TestApplication testApplication;

    @Inject AbsDataSource<Category> categoryAbsDataSource;
    @Inject AbsDataSource<Entry> entryAbsDataSource;

    @Before
    public void setup() {
        testApplication = new TestApplication();
        testApplication.inject(this);
    }

    @Test
    public void observersAreNotifiedOnDataInsert() throws InterruptedException {
        final CountDownLatch callbackLatch = new CountDownLatch(1);
        AbsDataSource.Observer observer = new AbsDataSource.Observer() {
            @Override
            public void onDataSourceChanged() {
                callbackLatch.countDown();
            }
        };

        categoryAbsDataSource.registerObserver(observer);

        categoryAbsDataSource.insert(new Category("test"));

        callbackLatch.await(DEFAULT_LATCH_WAIT, TimeUnit.MILLISECONDS);
        assertEquals("Observer was not notified", 0, callbackLatch.getCount());
    }

    @Test
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

        callbackLatch.await(DEFAULT_LATCH_WAIT, TimeUnit.MILLISECONDS);
        assertEquals("Observer was not notified", 0, callbackLatch.getCount());
    }

    @Test
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
}