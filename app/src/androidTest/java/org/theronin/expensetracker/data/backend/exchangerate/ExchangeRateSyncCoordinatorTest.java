package org.theronin.expensetracker.data.backend.exchangerate;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.theronin.expensetracker.data.Contract.EntryView;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.theronin.expensetracker.data.backend.exchangerate.ExchangeRateSyncCoordinator.BACKOFF_RATE;
import static org.theronin.expensetracker.data.backend.exchangerate.ExchangeRateSyncCoordinator.BASE_BACKOFF;
import static org.theronin.expensetracker.testutils.Constants.JAN_1_2000;
import static org.theronin.expensetracker.testutils.Constants.JAN_2_2000;
import static org.theronin.expensetracker.testutils.Constants.JAN_3_2000;
import static org.theronin.expensetracker.testutils.Constants.JAN_4_2000;
import static org.theronin.expensetracker.testutils.Constants.JAN_5_2000;
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

    @Test @SmallTest
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

    @Test @SmallTest
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

    @Test @SmallTest
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

    @Test @SmallTest
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

    @Test @SmallTest
    public void doesntTriggerDownloaderIfAllEntriesHaveTheSameCurrency() {
        when(entryDataSourceIsQueried()).thenReturn(new ArrayList<Entry>());
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());
        syncCoordinator.downloadExchangeRates();
        verifyNoMoreInteractions(downloader);
    }

    @Test @SmallTest
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

    @Test @SmallTest
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

    @Test @SmallTest
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

    @Test @SmallTest
    public void ensureFailedRatesGetIncrementedUp() {
        int testIncrements = 10;
        for (int i = 0; i < testIncrements + 1; i++) {
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

            if (i < testIncrements) {
                //Wouldn't be called on the last run through
                syncCoordinator.onDownloadComplete(new ArrayList<ExchangeRate>());
            }

            if (i >= testIncrements) {
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

    @Test @SmallTest
    public void exRatesWithFailedAttemptsShouldWaitTheAppropriateBackoffTime() {
        List<Entry> entriesFromDatabaseWithDifferentCurrency = Arrays.asList(
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_3_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_4_2000, -1, null, new Currency("EUR")),
                new Entry(null, JAN_5_2000, -1, null, new Currency("EUR"))
                );

        when(entryDataSourceIsQueried()).thenReturn(entriesFromDatabaseWithDifferentCurrency);

        long backOffAfterOneAttempt = BASE_BACKOFF * (long) Math.pow(BACKOFF_RATE, 0) + 1000L; //1:01 minutes
        long backOffAfterTwoAttempts = BASE_BACKOFF * (long) Math.pow(BACKOFF_RATE, 1) + 1000L; //2:01 minutes

        long durationLessThanBackOffAfterOneAttempt = backOffAfterOneAttempt / 2; //~30 secs
        long durationBetweenFirstAndSecondBackoff =
                (backOffAfterTwoAttempts - backOffAfterOneAttempt) / 2 + backOffAfterOneAttempt; //~90 secs

        List<ExchangeRate> alreadyDownloadedExchangeRates = Arrays.asList(
                //Shouldn't be downloaded again
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, System.currentTimeMillis() - durationLessThanBackOffAfterOneAttempt, 1),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, System.currentTimeMillis() - durationLessThanBackOffAfterOneAttempt, 1),
                //Should be downloaded again
                new ExchangeRate(-1, "EUR", JAN_3_2000, -1, System.currentTimeMillis() - backOffAfterOneAttempt, 1),
                new ExchangeRate(-1, "AUD", JAN_3_2000, -1, System.currentTimeMillis() - backOffAfterOneAttempt, 1),
                //Shouldn't be downloaded again
                new ExchangeRate(-1, "EUR", JAN_4_2000, -1, System.currentTimeMillis() - durationBetweenFirstAndSecondBackoff, 2),
                new ExchangeRate(-1, "AUD", JAN_4_2000, -1, System.currentTimeMillis() - durationBetweenFirstAndSecondBackoff, 2),
                //Should be downloaded again
                new ExchangeRate(-1, "EUR", JAN_5_2000, -1, System.currentTimeMillis() - backOffAfterTwoAttempts, 2),
                new ExchangeRate(-1, "AUD", JAN_5_2000, -1, System.currentTimeMillis() - backOffAfterTwoAttempts, 2)
        );

        when(exchangeRateSourceIsQueried()).thenReturn(alreadyDownloadedExchangeRates);

        Set<String> expectedDatesToBeRequested = new HashSet<>(Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000),
                DateUtils.getStorageFormattedDate(JAN_3_2000),
                DateUtils.getStorageFormattedDate(JAN_5_2000)
        ));

        Set<String> expectedCodesToBeRequested = new HashSet<>(Arrays.asList(
                "EUR",
                "AUD"
        ));

        syncCoordinator.downloadExchangeRates();

        verify(downloader).downloadExchangeRates(setContainsAll(expectedDatesToBeRequested), setContainsAll(expectedCodesToBeRequested));
    }
}