package org.theronin.expensetracker.data.backend;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.backend.ExchangeRateSyncCoordinator.CodeDatePair;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DataSourceExchangeRate;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.theronin.expensetracker.testutils.Constants.JAN_1_2000;
import static org.theronin.expensetracker.testutils.Constants.JAN_2_2000;
import static org.theronin.expensetracker.testutils.Constants.JAN_3_2000;
import static org.theronin.expensetracker.testutils.MockitoMatchers.containsAllExchangeRates;
import static org.theronin.expensetracker.testutils.MockitoMatchers.setContainsAll;

@RunWith(AndroidJUnit4.class)
public class ExchangeRateSyncCoordinatorTest {

    private DataSourceEntry entryAbsDataSource;
    private DataSourceExchangeRate exchangeRateAbsDataSource;
    private ExchangeRateDownloader downloader;
    private Currency homeCurrency;

    private ExchangeRateSyncCoordinator syncCoordinator;

    @Before
    public void setup() {
        entryAbsDataSource = mock(DataSourceEntry.class);
        doNothing().when(entryAbsDataSource).registerObserver(any(AbsDataSource.Observer.class));

        exchangeRateAbsDataSource = mock(DataSourceExchangeRate.class);
        downloader = mock(ExchangeRateDownloader.class);
        homeCurrency = new Currency("AUD", "$", "Australian Dollar");

        syncCoordinator = new ExchangeRateSyncCoordinator(
                entryAbsDataSource, exchangeRateAbsDataSource, downloader, homeCurrency);

        verify(downloader).setCallback(syncCoordinator);
    }

    @Test
    public void exchangeRatesShouldntBeDownloadedIfTheyAreAlreadyDownloaded() {
        List<Entry> entriesThatHaveADifferentCurrencyFromHome = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR")));

        when(entryDataSourceIsQueried()).thenReturn(entriesThatHaveADifferentCurrencyFromHome);

        //Looks like we already have exchange rate data for the 2nd of Jan
        List<ExchangeRate> ratesFoundFromDatabase = Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0)
        );

        when(exchangeRateSourceIsQueried()).thenReturn(ratesFoundFromDatabase);

        //should only need to query for the 1st of Jan
        Set<String> datesToDownload = new HashSet<>(Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000)
        ));

        Set<String> codesToDownload = new HashSet<>(Arrays.asList(
                "EUR",
                "AUD"
        ));

        syncCoordinator.downloadExchangeRates();

        verify(downloader).downloadExchangeRates(
                setContainsAll(datesToDownload),
                setContainsAll(codesToDownload));
    }
    //TODO test the above scenario, but inverted for testing currency (you know it'll fail, because
    //TODO we can't remove codes like we can dates, but check it anyway)

    @Test
    public void queriesAnyEntriesWhereCurrenciesDontMatch_AndCallsTheDownloader() {
        List<Entry> entriesThatHaveADifferentCurrencyFromHome = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR")));

        when(entryDataSourceIsQueried()).thenReturn(entriesThatHaveADifferentCurrencyFromHome);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.downloadExchangeRates();

        Set<String> datesToDownload = new HashSet<>(Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000),
                DateUtils.getStorageFormattedDate(JAN_2_2000)
        ));

        Set<String> codesToDownload = new HashSet<>(Arrays.asList(
                "EUR",
                "AUD"
        ));

        verify(downloader).downloadExchangeRates(
                setContainsAll(datesToDownload),
                setContainsAll(codesToDownload)
        );
    }

    @Test
    public void doesntRequestDuplicateExchangeRates() {
        //If there are many entries in a foreign currency for the same day, we don't want to pass
        //a new exchange rate request for each one. The same exchange rate will cover them all
        List<Entry> entriesResultingInDuplicateExRates = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")));

        when(entryDataSourceIsQueried()).thenReturn(entriesResultingInDuplicateExRates);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.downloadExchangeRates();

        Set<String> datesToDownload = new HashSet<>(Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000)
        ));

        Set<String> codesToDownload = new HashSet<>(Arrays.asList(
                "EUR",
                "AUD"
        ));

        verify(downloader).downloadExchangeRates(
                setContainsAll(datesToDownload),
                setContainsAll(codesToDownload)
        );
    }

    @Test
    public void thirdExchangeRateDoesntCreateDuplicateRequests() {
        //The case where there is more than one exRate for a given day. This should then create 3
        //exchange rates, one for each currency on that date
        List<Entry> entriesFromDatabase = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_1_2000, -1, null, new Currency("JPY")));

        when(entryDataSourceIsQueried()).thenReturn(entriesFromDatabase);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.downloadExchangeRates();

        Set<String> datesToDownload = new HashSet<>(Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000)
        ));

        Set<String> codesToDownload = new HashSet<>(Arrays.asList(
                "EUR",
                "AUD",
                "JPY"
        ));

        verify(downloader).downloadExchangeRates(
                setContainsAll(datesToDownload),
                setContainsAll(codesToDownload)
        );
    }

    @Test
    public void doesntTriggerDownloaderIfAllEntriesHaveTheSameCurrency() {
        when(entryDataSourceIsQueried()).thenReturn(new ArrayList<Entry>());
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());
        syncCoordinator.downloadExchangeRates();
        verifyNoMoreInteractions(downloader);
    }

    @Test
    public void doesntTriggerDownloaderIfAllExRatesAreDownloaded() {
        List<Entry> entriesThatHaveADifferentCurrencyFromHome = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR")));

        when(entryDataSourceIsQueried()).thenReturn(entriesThatHaveADifferentCurrencyFromHome);

        //Looks like we already have exchange rate data for the 2nd of Jan
        List<ExchangeRate> ratesFoundFromDatabase = Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0)
        );

        when(exchangeRateSourceIsQueried()).thenReturn(ratesFoundFromDatabase);
        syncCoordinator.downloadExchangeRates();
        verifyNoMoreInteractions(downloader);
    }

    @Test
    public void whenDownloadCompletesTheCoordinatorSavesTheExchangeRates() {

        List<Entry> entryWithDifferingExchangeRate = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")));

        when(entryDataSourceIsQueried()).thenReturn(entryWithDifferingExchangeRate);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.downloadExchangeRates();

        List<ExchangeRate> downloadedExchangeRates = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_1_2000, 1.2439, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_1_2000, 0.9239, -1, 0));

        when(exchangeRateAbsDataSource.bulkInsert(downloadedExchangeRates)).thenReturn(downloadedExchangeRates);
        syncCoordinator.onDownloadComplete(downloadedExchangeRates);

        verify(exchangeRateAbsDataSource).bulkInsert(containsAllExchangeRates(downloadedExchangeRates));
    }

    @Test
    public void ensureLargeRequestsAreThrottled() {
        int numDays = 1000;
        int expectedRates = numDays * 2; //two currencies per day
        int testBatchSize = 100;
        int expectedBatches = (int) Math.ceil((double) expectedRates / testBatchSize);
        List<Entry> largeNumEntriesWithNoExchangeDate = createEntriesSpanningDays(numDays);

        when(entryDataSourceIsQueried()).thenReturn(largeNumEntriesWithNoExchangeDate);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.setDownloadBatchSize(testBatchSize);
        syncCoordinator.downloadExchangeRates();

        for (int i = 0; i < expectedBatches; i++) {
            //Pass in all missing exchange rates. This test doesn't care if they fail.
            syncCoordinator.onDownloadComplete(new ArrayList<ExchangeRate>());
        }

        verify(downloader, atLeast(expectedBatches)).downloadExchangeRates(
                anySetOf(String.class),
                anySetOf(String.class));
    }

    private List<Entry> createEntriesSpanningDays(int numDays) {
        List<Entry> entries = new ArrayList<>();
        long dayInMillis = 24L * 60L * 60L * 1000L;
        long startDate = DateUtils.getUtcTime("2000-01-01");
        for (int i = 0; i < numDays; i++) {
            entries.add(new Entry(null, null, (startDate + i * dayInMillis), 100, null, new Currency("EUR")));
        }
        return entries;
    }

    @Test
    public void ensureMultipleBatchesDownloadInDescendingDateOrder() {
        int testBatchLimit = 2; //batch limit for each test entry (each one should produce 2 exRates)

        List<Entry> unorderedEntriesToBeFetchedFromDataSource = Arrays.asList(
                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_3_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR"))
        );

        when(entryDataSourceIsQueried()).thenReturn(unorderedEntriesToBeFetchedFromDataSource);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        List<CodeDatePair> ratesInExpectedDownloadOrder = Arrays.asList(
                new CodeDatePair("EUR", JAN_3_2000),
                new CodeDatePair("AUD", JAN_3_2000),
                new CodeDatePair("EUR", JAN_2_2000),
                new CodeDatePair("AUD", JAN_2_2000),
                new CodeDatePair("EUR", JAN_1_2000),
                new CodeDatePair("AUD", JAN_1_2000)
        );

        syncCoordinator.setDownloadBatchSize(testBatchLimit);

        for (int i = 0; i < ratesInExpectedDownloadOrder.size(); i = i + 2) {
            CodeDatePair rate1 = ratesInExpectedDownloadOrder.get(i);
            CodeDatePair rate2 = ratesInExpectedDownloadOrder.get(i + 1);

            Set<String> expectedDatesToBeRequested = new HashSet<>(Arrays.asList(rate1.date));

            Set<String> expectedCodesToBeRequested = new HashSet<>(Arrays.asList(rate1.code, rate2.code));

            if (i == 0) {
                //Trigger first batch
                syncCoordinator.downloadExchangeRates();
            } else {
                String lastDate = ratesInExpectedDownloadOrder.get(i - 2).date;
                //Confirm the first and second batch, while also triggering the second and third batches
                verifyDownloadCompleteCycle(DateUtils.getUtcTime(lastDate));
            }

            verify(downloader).downloadExchangeRates(setContainsAll(expectedDatesToBeRequested), setContainsAll(expectedCodesToBeRequested));
        }

        //Confirm third batch saved properly
        verifyDownloadCompleteCycle(JAN_1_2000);
    }

    private void verifyDownloadCompleteCycle(long date) {
        List<ExchangeRate> downloadedExchangeRates = Arrays.asList(
                new ExchangeRate(-1, "EUR", date, 1, -1, 0),
                new ExchangeRate(-1, "AUD", date, 1, -1, 0));

        when(exchangeRateAbsDataSource.bulkInsert(downloadedExchangeRates)).thenReturn(downloadedExchangeRates);
        syncCoordinator.onDownloadComplete(downloadedExchangeRates);
        verify(exchangeRateAbsDataSource).bulkInsert(containsAllExchangeRates(downloadedExchangeRates));
    }

    @Test
    public void ifExRateMissingAfterDownload_CreateAnAttemptedExRate() {
        //some rates for the entries
        double rate_1p2340 = 1.2340;
        double rate_0p9345 = 0.9345;

        List<Entry> entriesFromDatabaseWithDifferentCurrency = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR")));

        when(entryDataSourceIsQueried()).thenReturn(entriesFromDatabaseWithDifferentCurrency);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.downloadExchangeRates();

        //uh oh, the other date is missing...
        List<ExchangeRate> downloadedExRates = new ArrayList<>(Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_1_2000, rate_0p9345, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, rate_1p2340, -1, 0)));

        syncCoordinator.onDownloadComplete(downloadedExRates);

        //We expect to see that the number of download attempts goes up for the missing exchange rates
        //And that they get saved with a negative usdRate and a last download attempt timestamp
        List<ExchangeRate> expectedSavedExRates = new ArrayList<>(Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_1_2000, rate_0p9345, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, rate_1p2340, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, System.currentTimeMillis(), 1),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, System.currentTimeMillis(), 1)));

        verify(exchangeRateAbsDataSource).bulkInsert(containsAllExchangeRates(expectedSavedExRates));
    }

    @Test
    public void ensureFailedRatesGetIncrementedUpToMaxDownloadAttemptsCount() {
        for (int i = 0; i < ExchangeRateSyncCoordinator.MAX_DOWNLOAD_ATTEMPTS + 1; i++) {
            if (i > 0) {
                setup(); //reset the coordinator (each run might be a long time apart here);
            }

            List<Entry> entriesFromDatabaseWithDifferentCurrency = Arrays.asList(
                    new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")));

            when(entryDataSourceIsQueried()).thenReturn(entriesFromDatabaseWithDifferentCurrency);

            List<ExchangeRate> previouslyDownloadedRates = i == 0 ? new ArrayList<ExchangeRate>() : Arrays.asList(
                    new ExchangeRate(-1, "EUR", JAN_1_2000, -1, 10, i),
                    new ExchangeRate(-1, "AUD", JAN_1_2000, -1, 10, i));

            when(exchangeRateSourceIsQueried()).thenReturn(previouslyDownloadedRates);
            syncCoordinator.downloadExchangeRates();
            verify(exchangeRateAbsDataSource).query();

            if (i < ExchangeRateSyncCoordinator.MAX_DOWNLOAD_ATTEMPTS) {
                //Wouldn't be called on the last run through
                syncCoordinator.onDownloadComplete(new ArrayList<ExchangeRate>());
            }

            if (i >= ExchangeRateSyncCoordinator.MAX_DOWNLOAD_ATTEMPTS) {
                //we expect to see that nothing happened
                verifyNoMoreInteractions(exchangeRateAbsDataSource);
            } else {
                //We expect to see that the number of download attempts goes up for the missing exchange rates
                //And that they get saved with a negative usdRate and a last download attempt timestamp
                List<ExchangeRate> expectedSavedExRates = Arrays.asList(
                        new ExchangeRate(-1, "EUR", JAN_1_2000, -1, System.currentTimeMillis(), i + 1),
                        new ExchangeRate(-1, "AUD", JAN_1_2000, -1, System.currentTimeMillis(), i + 1));

                verify(exchangeRateAbsDataSource).bulkInsert(containsAllExchangeRates(expectedSavedExRates));
            }
        }
    }

    private List<Entry> entryDataSourceIsQueried() {
        return entryAbsDataSource.query(EntryView.COL_CURRENCY_CODE + " != ?", new String[]{homeCurrency.code}, null);
    }

    private List<ExchangeRate> exchangeRateSourceIsQueried() {
        return exchangeRateAbsDataSource.query();
    }

    //TODO last (I hope) test - check time spans between tests
    @Test
    public void entriesWithOneFailedAttemptShouldntBeDownloadedAgainFor24Hours() {
        List<Entry> entriesFromDatabaseWithDifferentCurrency = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR")));

        when(entryDataSourceIsQueried()).thenReturn(entriesFromDatabaseWithDifferentCurrency);

        long aFewHoursAgo = System.currentTimeMillis() - 3L * 60L * 60L * 1000L;
        long oneDayAgo = System.currentTimeMillis() - ExchangeRateSyncCoordinator.BACKOFF_FIRST_ATTEMPT;

        List<ExchangeRate> alreadyDownloadedExchangeRates = Arrays.asList(
                //Should be downloaded again
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, oneDayAgo, 1),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, oneDayAgo, 1),
                //Shouldn't be downloaded again
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, aFewHoursAgo, 1),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, aFewHoursAgo, 1)
        );

        when(exchangeRateSourceIsQueried()).thenReturn(alreadyDownloadedExchangeRates);

        Set<String> expectedDatesToBeRequested = new HashSet<>(Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000)
        ));

        Set<String> expectedCodesToBeRequested = new HashSet<>(Arrays.asList(
                "EUR",
                "AUD"
        ));

        syncCoordinator.downloadExchangeRates();

        verify(downloader).downloadExchangeRates(setContainsAll(expectedDatesToBeRequested), setContainsAll(expectedCodesToBeRequested));
    }

    @Test
    public void entriesWithTwoFailedAttemptsShouldntBeDownloadedAgainFor1Week() {
        List<Entry> entriesFromDatabaseWithDifferentCurrency = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR")));

        when(entryDataSourceIsQueried()).thenReturn(entriesFromDatabaseWithDifferentCurrency);

        long aFewDaysAgo = System.currentTimeMillis() - 3L * 24L * 60L * 60L * 1000L;
        long oneWeekAgo = System.currentTimeMillis() - ExchangeRateSyncCoordinator.BACKOFF_SECOND_ATTEMPT;

        List<ExchangeRate> alreadyDownloadedExchangeRates = Arrays.asList(
                //Should be downloaded again
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, oneWeekAgo, 2),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, oneWeekAgo, 2),
                //Shouldn't be downloaded again
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, aFewDaysAgo, 2),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, aFewDaysAgo, 2)
        );

        when(exchangeRateSourceIsQueried()).thenReturn(alreadyDownloadedExchangeRates);

        Set<String> expectedDatesToBeRequested = new HashSet<>(Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000)
        ));

        Set<String> expectedCodesToBeRequested = new HashSet<>(Arrays.asList(
                "EUR",
                "AUD"
        ));

        syncCoordinator.downloadExchangeRates();

        verify(downloader).downloadExchangeRates(setContainsAll(expectedDatesToBeRequested), setContainsAll(expectedCodesToBeRequested));
    }

}