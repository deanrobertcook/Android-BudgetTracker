package org.theronin.expensetracker.data.sync;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ParseRemoteSyncTest {

    @Before
    public void setup() {

    }

    @Test
    public void successfulParseSyncTriggersCustomCallback() {
        ParseRemoteSync parseRemoteSync = new ParseRemoteSync();
    }
}
