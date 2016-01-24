package org.theronin.expensetracker.data.backend;

import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Test;
import org.theronin.expensetracker.data.backend.entry.EntrySyncCoordinator;
import org.theronin.expensetracker.data.backend.entry.ParseEntryRemoteSync;
import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.data.backend.exchangerate.ExchangeRateSyncCoordinator;
import org.theronin.expensetracker.data.backend.exchangerate.ParseExchangeRateDownloader;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DataSourceExchangeRate;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.theronin.expensetracker.data.Contract.EntryView.COL_CURRENCY_CODE;
import static org.theronin.expensetracker.testutils.Constants.JAN_1_2011;

public class ParseLimitTest {

//    @Test @LargeTest
    public void testEntryLimit() {
        int numEntriesToSync = 2000;
        AbsDataSource<Entry> absDataSource = mock(DataSourceEntry.class);
        ParseEntryRemoteSync remoteSync = new ParseEntryRemoteSync();
        EntrySyncCoordinator coordinator = new EntrySyncCoordinator(absDataSource, remoteSync);

        coordinator.syncEntries(createEntries(numEntriesToSync));
    }

    private List<Entry> createEntries(int numEntries) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < numEntries; i++) {
            long days = (long) i * 24L * 60L * 60L * 1000L;
            entries.add(new Entry(null, SyncState.NEW, JAN_1_2011 + days, 100, new Category("test"), new Currency("EUR")));
        }
        return entries;
    }

    @Test
    @LargeTest
    public void testExchangeRateLimit() {
        int numDatesTowDownload = 15; //exeeds the limit of 10 fresh ex rates

        Currency homeCurrency = new Currency("AUD");

        AbsDataSource<Entry> entryAbsDataSource = mock(DataSourceEntry.class);
        when(entryAbsDataSource.query(COL_CURRENCY_CODE + " != ?", new String[]{homeCurrency.code}, null)).thenReturn(createEntries(numDatesTowDownload));

        AbsDataSource<ExchangeRate> exchangeRateAbsDataSource = mock(DataSourceExchangeRate.class);
        when(exchangeRateAbsDataSource.query()).thenReturn(new ArrayList<ExchangeRate>());

        ParseExchangeRateDownloader downloader = new ParseExchangeRateDownloader();


        ExchangeRateSyncCoordinator coordinator = new ExchangeRateSyncCoordinator(entryAbsDataSource, exchangeRateAbsDataSource, downloader, homeCurrency);
        coordinator.downloadExchangeRates();
    }

}
