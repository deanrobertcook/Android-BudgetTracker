package org.theronin.expensetracker.model;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;
import org.theronin.expensetracker.data.backend.entry.SyncState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.theronin.expensetracker.testutils.Constants.JAN_1_2000;
import static org.theronin.expensetracker.testutils.Constants.JAN_2_2000;
import static org.theronin.expensetracker.testutils.Constants.JAN_3_2000;

public class EntryTest {
    @Test @SmallTest
    public void equalityTestShouldFailIfGlobalIdsDiffer() {
        Entry entry1 = new Entry("abc", 0, 0, null, null);
        Entry entry2 = new Entry("xyz", 0, 0, null, null);

        assertFalse(entry1.equals(entry2));
    }

    @Test @SmallTest
    public void equalityTestShouldPassIfGlobalIdsAreTheSameButIdsDiffer() {
        Entry entry1 = new Entry(2, "abc", SyncState.SYNCED, 0, 0, null, null);
        Entry entry2 = new Entry(1, "abc", SyncState.SYNCED, 0, 0, null, null);

        assertTrue(entry1.equals(entry2));
    }

    @Test @SmallTest
    public void equalityTestShouldFailIfNoGlobalIdAndIdsDiffer() {
        Entry entry1 = new Entry(2, null, SyncState.SYNCED, 0, 0, null, null);
        Entry entry2 = new Entry(1, null, SyncState.SYNCED, 0, 0, null, null);

        assertFalse(entry1.equals(entry2));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionShouldBeThrownIfIdsEqualButContentsAreDifferent() {
        Entry entry1 = new Entry(1, null, SyncState.SYNCED, 1, 1, new Category("test1"), new Currency("AUD"));
        Entry entry2 = new Entry(1, null, SyncState.SYNCED, 2, 2, new Category("test2"), new Currency("EUR"));

        entry1.equals(entry2);
    }

    @Test @SmallTest
    public void sortingEntriesShouldReturnThemInDateOrder() {
        List<Entry> entryList = Arrays.asList(
                new Entry(1, null, SyncState.SYNCED, JAN_1_2000, 1, new Category("test1"), new Currency("AUD")),
                new Entry(1, null, SyncState.SYNCED, JAN_3_2000, 2, new Category("test2"), new Currency("EUR")),
                new Entry(1, null, SyncState.SYNCED, JAN_2_2000, 2, new Category("test2"), new Currency("EUR"))
        );

        Collections.sort(entryList);

        assertEquals(JAN_3_2000, entryList.get(0).utcDate);
        assertEquals(JAN_2_2000, entryList.get(1).utcDate);
        assertEquals(JAN_1_2000, entryList.get(2).utcDate);
    }
}
