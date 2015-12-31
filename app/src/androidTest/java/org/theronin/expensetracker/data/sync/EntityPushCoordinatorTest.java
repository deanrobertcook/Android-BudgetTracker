package org.theronin.expensetracker.data.sync;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.theronin.expensetracker.data.TestApplication;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.sync.EntityPushCoordinator.EntitySaver;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class EntityPushCoordinatorTest {

    private static final long DEFAULT_LATCH_WAIT = 2000;
    @Inject AbsDataSource<Entry> entryAbsDataSource;

    @Before
    public void setup() {
        TestApplication application = new TestApplication();
        application.inject(this);
    }

    @Test
    public void syncHelperCorrectlySortsEntries() throws InterruptedException {
        List<Entry> allEntries = createRangeOfEntries();

        final CountDownLatch lock = new CountDownLatch(4);


        EntitySaver<Entry> fakeEntitySaver = new EntitySaver<Entry>() {
            @Override
            public void addEntriesToRemote(List<Entry> entries) {
                assertEquals("Count of entries passed for adding was wrong", 2, entries.size());
                lock.countDown();
            }

            @Override
            public void updateEntriesOnRemote(List<Entry> entries) {
                assertEquals("Count of entries passed for updating was wrong", 3, entries.size());
                lock.countDown();
            }

            @Override
            public void deleteEntriesFromRemote(List<Entry> entries) {
                assertEquals("Count of entries passed for remote deletion was wrong", 1, entries.size());
                lock.countDown();
            }

            @Override
            public void deleteEntriesLocally(List<Entry> entries) {
                assertEquals("Count of entries passed for local deletion was wrong", 2, entries.size());
                lock.countDown();
            }
        };

        EntityPushCoordinator entityPushCoordinator = new EntityPushCoordinator(fakeEntitySaver);
        entityPushCoordinator.syncEntries(allEntries);
        
        lock.await(DEFAULT_LATCH_WAIT, TimeUnit.MILLISECONDS);
        assertEquals("Not all callbacks were triggered", 0, lock.getCount());
    }

    @Test(expected = IllegalStateException.class)
    public void entriesMarkedWithNew_andThatHaveGlobalId_shouldThrowException() {
        Entry erroneousEntry = new Entry(
                "123456", SyncState.NEW, System.currentTimeMillis(), 100L, new Category("Test"), new Currency("AUD"));

        EntitySaver<Entry> fakeEntitySaver = new EntitySaver<Entry>() {
            @Override
            public void addEntriesToRemote(List<Entry> entries) {

            }

            @Override
            public void updateEntriesOnRemote(List<Entry> entries) {

            }

            @Override
            public void deleteEntriesFromRemote(List<Entry> entries) {

            }

            @Override
            public void deleteEntriesLocally(List<Entry> entries) {

            }
        };

        EntityPushCoordinator<Entry> entityPushCoordinator = new EntityPushCoordinator<>(fakeEntitySaver);
        entityPushCoordinator.syncEntries(Arrays.asList(erroneousEntry));
    }

    private List<Entry> createRangeOfEntries() {
        String someGlobalId = "123456";
        long someDate = System.currentTimeMillis();
        long someAmount = 100L;
        Category someCategory = new Category("Test");
        //Needs to be one of the supported currencies
        Currency someCurrency = new Currency("AUD");

       return Arrays.asList(
                //Some that have not been synced (no globalId and SyncState = NEW
                new Entry(null, SyncState.NEW, someDate, someAmount, someCategory, someCurrency),
                new Entry(null, SyncState.NEW, someDate, someAmount, someCategory, someCurrency),

                //Some that have been synced before, but we should update the server
                new Entry(someGlobalId, SyncState.UPDATED, someDate, someAmount, someCategory, someCurrency),
                new Entry(someGlobalId, SyncState.UPDATED, someDate, someAmount, someCategory, someCurrency),
                new Entry(someGlobalId, SyncState.UPDATED, someDate, someAmount, someCategory, someCurrency),

                //Some that have been marked as deleted on the client to be synced with the backend
                new Entry(someGlobalId, SyncState.MARKED_AS_DELETED, someDate, someAmount, someCategory, someCurrency),

                //Some that the backend has confirmed to have deleted
                new Entry(someGlobalId, SyncState.DELETE_SYNCED, someDate, someAmount, someCategory, someCurrency),
                new Entry(someGlobalId, SyncState.DELETE_SYNCED, someDate, someAmount, someCategory, someCurrency),

                //Some that have been synced and don't need updating
                new Entry(someGlobalId, SyncState.SYNCED, someDate, someAmount, someCategory, someCurrency),
                new Entry(someGlobalId, SyncState.SYNCED, someDate, someAmount, someCategory, someCurrency),
                new Entry(someGlobalId, SyncState.SYNCED, someDate, someAmount, someCategory, someCurrency),
                new Entry(someGlobalId, SyncState.SYNCED, someDate, someAmount, someCategory, someCurrency)
        );
    }

}
