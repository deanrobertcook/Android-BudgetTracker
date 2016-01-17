package org.theronin.expensetracker.data.backend;

import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
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
import org.theronin.expensetracker.utils.DebugUtils;
import org.theronin.expensetracker.utils.ExchangeRateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ExchangeRateSyncCoordinatorTest {

    public static final long JAN_1_2000 = DateUtils.getUtcTime("2000-01-01");
    public static final long JAN_2_2000 = DateUtils.getUtcTime("2000-01-02");

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

    //TODO get this test working
//    @Test
//    public void ifExRateMissingAfterDownload_CreateAnAttemptedExRate() {
//        //some rates for the entries
//        double rate_1p2340 = 1.2340;
//        double rate_0p9345 = 0.9345;
//
//        List<Entry> entriesFromDatabaseWithDifferentCurrency = Arrays.asList(
//                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR", "€", "Euro")),
//                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR", "€", "Euro")));
//
//        when(entryDataSourceIsQueried()).thenReturn(entriesFromDatabaseWithDifferentCurrency);
//        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());
//
//        syncCoordinator.downloadExchangeRates();
//
//        //uh oh, the other date is missing...
//        List<ExchangeRate> downloadedExRates = new ArrayList<>(Arrays.asList(
//                new ExchangeRate(-1, "EUR", JAN_1_2000, rate_0p9345, -1, 0),
//                new ExchangeRate(-1, "AUD", JAN_1_2000, rate_1p2340, -1, 0)));
//
//        syncCoordinator.onDownloadComplete(downloadedExRates);
//
//        //We expect to see that the number of download attempts goes up for the missing exchange rates
//        //And that they get saved with a negative usdRate and a last download attempt timestamp
//        List<ExchangeRate> expectedSavedExRates = new ArrayList<>(Arrays.asList(
//                new ExchangeRate(-1, "EUR", JAN_1_2000, rate_0p9345, -1, 0),
//                new ExchangeRate(-1, "AUD", JAN_1_2000, rate_1p2340, -1, 0),
//                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, System.currentTimeMillis(), 1),
//                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, System.currentTimeMillis(), 1)));
//
//        verify(exchangeRateAbsDataSource).bulkInsert(containsAllExchangeRates(expectedSavedExRates));
//    }

    //TODO get this test working
    @Test
    public void ensureLargeRequestsAreThrottled() {
        List<Entry> largeNumEntriesWithNoExchangeDate = createEntriesSpanningDays(1000);

        when(entryDataSourceIsQueried()).thenReturn(largeNumEntriesWithNoExchangeDate);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.downloadExchangeRates();

        verify(downloader, atLeast(10)).downloadExchangeRates(
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

    private List<Entry> entryDataSourceIsQueried() {
        return entryAbsDataSource.query(EntryView.COL_CURRENCY_CODE + " != ?", new String[]{homeCurrency.code}, null);
    }

    private List<ExchangeRate> exchangeRateSourceIsQueried() {
        return exchangeRateAbsDataSource.query();
    }

    //TODO test what happens if you there are exchange rates that couldn't be downloaded last time
    //TODO test what happens if there is an exchange rate that has unsuccessfully been downloaded 3 times.

    /**
     * A matcher that takes two lists of Exchange rates and compares them by date (of the form
     * YYYY-MM-DD, currency code, usdRate, the last download attempt (within the nearest second) and
     * the number of counted download attempts.
     *
     * @param expectedList
     * @return
     */
    public static List<ExchangeRate> containsAllExchangeRates(final List<ExchangeRate> expectedList) {
        return argThat(new TypeSafeMatcher<List<ExchangeRate>>() {

            private final double RATE_EPSILON = 0.000001;
            private boolean sizeDiffers = false;
            private int expectedSize = 0;
            private int actualSize = 0;
            private ExchangeRate expected;
            private ExchangeRate actual;

            @Override
            protected boolean matchesSafely(List<ExchangeRate> actualList) {
                if ((expectedSize = expectedList.size()) != (actualSize = actualList.size())) {
                    sizeDiffers = true;
                    return false;
                }
                Collections.sort(expectedList, ExchangeRateUtils.comparator());
                Collections.sort(actualList, ExchangeRateUtils.comparator());

                for (int i = 0; i < expectedList.size(); i++) {
                    ExchangeRate expected = expectedList.get(i);
                    ExchangeRate actual = actualList.get(i);

                    long timeDiff = Math.abs(expected.getUtcLastUpdated() - actual.getUtcLastUpdated());

                    if (!expected.equals(actual) ||
                            expected.getDownloadAttempts() != actual.getDownloadAttempts() ||
                            timeDiff > 1000 ||
                            Math.abs(expected.getUsdRate() - actual.getUsdRate()) > RATE_EPSILON) {

                        this.expected = expected;
                        this.actual = actual;
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {

                if (sizeDiffers) {
                    description.appendText(String.format(
                            "The size of the two arrays did not match. expected: %d, actual: %d",
                            expectedSize, actualSize
                    ));
                    return;
                }

                description.appendText(String.format(
                        "The first encountered elements that differed were (expected, actual): \n %s \n\t %s",
                        expected.toString(), actual.toString()
                ));
            }
        });
    }

    public static Set<String> setContainsAll(final Set<String> expectedDates) {

        return argThat(new TypeSafeMatcher<Set<String>>() {

            private Set<String> actualDates;

            @Override
            protected boolean matchesSafely(final Set<String> actualDates) {
                this.actualDates = actualDates;
                return expectedDates.equals(actualDates);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Sets don't match");
                DebugUtils.printListString("expected", expectedDates);
                DebugUtils.printListString("actual", actualDates);
            }
        });
    }
}