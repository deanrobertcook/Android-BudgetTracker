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
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.ExchangeRateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ExchangeRateSyncCoordinatorTest {

    public static final String TEST_GLOBAL_ID = "12345678abc";
    public static final Long TEST_AMOUNT = 550L; // $5.50
    public static final Category TEST_CATEGORY = new Category("Test");

    public static final long JAN_1_2000 = 946681201000L; //2000-01-01
    public static final long JAN_2_2000 = JAN_1_2000 + 86400000L; //2000-01-02

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
    }

    @Test
    public void registersItselfToEntryDataSource() {
        verify(entryAbsDataSource).registerObserver(syncCoordinator);
    }


    //TODO test that rates don't get downloaded again if they already have been!
    @Test
    public void exchangeRatesShouldntBeDownloadedTwice() {
        List<Entry> entriesThatHaveADifferentCurrencyFromHome = Arrays.asList(
                new Entry(TEST_GLOBAL_ID, JAN_1_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")),
                new Entry(TEST_GLOBAL_ID, JAN_2_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")));

        when(entryDataSourceIsQueried()).thenReturn(entriesThatHaveADifferentCurrencyFromHome);

        //Looks like we already have exchange rate data for the 2nd of Jan
        List<ExchangeRate> ratesFoundFromDatabase = Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0)
        );

        when(exchangeRateSourceIsQueried()).thenReturn(ratesFoundFromDatabase);

        List<ExchangeRate> ratesLeftToDownload = Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0)
        );

        syncCoordinator.onDataSourceChanged();

        verify(downloader).downloadExchangeRates(containsAllExchangeRates(ratesLeftToDownload));
    }

    @Test
    public void queriesAnyEntriesWhereCurrenciesDontMatch_AndCallsTheDownloader() {
        List<Entry> entriesThatHaveADifferentCurrencyFromHome = Arrays.asList(
                new Entry(TEST_GLOBAL_ID, JAN_1_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")),
                new Entry(TEST_GLOBAL_ID, JAN_2_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")));

        when(entryDataSourceIsQueried()).thenReturn(entriesThatHaveADifferentCurrencyFromHome);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.onDataSourceChanged();


        /*
        If entries have currencies that don't match the home currency, then we expect to see for
        every currency that differs and every day that we don't have in the data base a new
        exchange rate. I.e., we expect to see 2N exchange rates, where N is the number of entries
        with non matching currencies
        */
        List<ExchangeRate> toBeDownloadedExchangeRates = Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0)
        );

        verify(downloader).downloadExchangeRates(containsAllExchangeRates(toBeDownloadedExchangeRates));
    }

    @Test
    public void doesntTriggerDownloaderIfAllEntriesHaveTheSameCurrency() {
        when(entryDataSourceIsQueried()).thenReturn(new ArrayList<Entry>());
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());
        syncCoordinator.onDataSourceChanged();
        verifyZeroInteractions(downloader);
    }

    @Test
    public void doesntTriggerDownloaderIfAllExRatesAreDownloaded() {
        List<Entry> entriesThatHaveADifferentCurrencyFromHome = Arrays.asList(
                new Entry(TEST_GLOBAL_ID, JAN_1_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")),
                new Entry(TEST_GLOBAL_ID, JAN_2_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")));

        when(entryDataSourceIsQueried()).thenReturn(entriesThatHaveADifferentCurrencyFromHome);

        //Looks like we already have exchange rate data for the 2nd of Jan
        List<ExchangeRate> ratesFoundFromDatabase = Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0)
        );

        when(exchangeRateSourceIsQueried()).thenReturn(ratesFoundFromDatabase);
        syncCoordinator.onDataSourceChanged();
        verifyZeroInteractions(downloader);
    }

    @Test
    public void whenDownloadCompletesTheCoordinatorSavesTheExchangeRates() {

        List<Entry> entryWithDifferingExchangeRate = Arrays.asList(
                new Entry(TEST_GLOBAL_ID, JAN_1_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")));

        when(entryDataSourceIsQueried()).thenReturn(entryWithDifferingExchangeRate);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.onDataSourceChanged();

        List<ExchangeRate> downloadedExchangeRates = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_1_2000, 1.2439, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_1_2000, 0.9239, -1, 0));

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
                new Entry(null, JAN_1_2000, -1, null, new Currency("EUR", "€", "Euro")),
                new Entry(null, JAN_2_2000, -1, null, new Currency("EUR", "€", "Euro")));

        when(entryDataSourceIsQueried()).thenReturn(entriesFromDatabaseWithDifferentCurrency);
        when(exchangeRateSourceIsQueried()).thenReturn(new ArrayList<ExchangeRate>());

        syncCoordinator.onDataSourceChanged();

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

    private List<Entry> entryDataSourceIsQueried() {
        return entryAbsDataSource.query(EntryView.COL_CURRENCY_CODE + " != ?", new String[]{homeCurrency.code}, null);
    }

    private List<ExchangeRate> exchangeRateSourceIsQueried() {
        return exchangeRateAbsDataSource.query();
    }

    //TODO test what happens if a datasource change occurs while waiting for results...
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
}