package org.theronin.expensetracker.data;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceCategory;
import org.theronin.expensetracker.data.source.DataSourceCurrency;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DbHelper;
import org.theronin.expensetracker.data.sync.SyncState;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AbsDataSourceTest {

    private final int DEFAULT_LATCH_WAIT = 2000;

    Instrumentation instrumentation;
    DbHelper testDbHelper;

    @Before
    public void setup() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        testDbHelper = DbHelper.getInstance(instrumentation.getTargetContext(), null);
    }

    @Test
    public void observersAreNotifiedOnDataInsert() throws InterruptedException {
        AbsDataSource<Category> exampleDataSource = new DataSourceCategory(instrumentation.getTargetContext(), testDbHelper);

        final CountDownLatch callbackLatch = new CountDownLatch(1);
        AbsDataSource.Observer observer = new AbsDataSource.Observer() {
            @Override
            public void onDataSourceChanged() {
                callbackLatch.countDown();
            }
        };

        exampleDataSource.registerObserver(observer);

        exampleDataSource.insert(new Category("test"));

        callbackLatch.await(DEFAULT_LATCH_WAIT, TimeUnit.MILLISECONDS);
        assertEquals("Observer was not notified", 0, callbackLatch.getCount());
    }

    @Test
    public void observersAreNotifiedOnDataDeletion() throws InterruptedException {
        AbsDataSource<Entry> testDataSource = new DataSourceEntry(
                instrumentation.getTargetContext(), testDbHelper,
                new DataSourceCategory(instrumentation.getTargetContext(), testDbHelper),
                new DataSourceCurrency(instrumentation.getTargetContext(), testDbHelper)
        );

        final CountDownLatch callbackLatch = new CountDownLatch(1);

        long insertedId = testDataSource.insert(new Entry(100L, 100L, new Category("Test"), new Currency("AUD")));
        Entry insertedEntry = new Entry(insertedId, "xyz", SyncState.DELETE_SYNCED, 100L, 100L, new Category("Test"), new Currency("AUD"));

        AbsDataSource.Observer observer = new AbsDataSource.Observer() {
            @Override
            public void onDataSourceChanged() {
                callbackLatch.countDown();
            }
        };

        testDataSource.registerObserver(observer);
        testDataSource.delete(insertedEntry);

        callbackLatch.await(DEFAULT_LATCH_WAIT, TimeUnit.MILLISECONDS);
        assertEquals("Observer was not notified", 0, callbackLatch.getCount());
    }
}