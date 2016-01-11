package org.theronin.expensetracker.data.backend;

import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    @Test
    public void queriesAnyEntriesWhereCurrenciesDontMatch_AndCallsTheDownloader() {
        List<Entry> entriesThatHaveADifferentCurrencyFromHome = Arrays.asList(
                new Entry(TEST_GLOBAL_ID, JAN_1_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")),
                new Entry(TEST_GLOBAL_ID, JAN_2_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")));

        when(entryAbsDataSource.query(syncCoordinator.getQueryString(), new String[]{homeCurrency.code}, null))
                .thenReturn(entriesThatHaveADifferentCurrencyFromHome);

        syncCoordinator.onDataSourceChanged();

        //If entries have currencies that don't match, then we expect to see for every currency that differs
        //and every day that we don't have in the data base
        List<ExchangeRate> expectedRatesToRequestDownload = Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0)
        );

        verify(downloader).downloadExchangeRates(argThat(containsAllExchangeRates(expectedRatesToRequestDownload)));
    }

    @Test
    public void doesntTriggerDownloaderIfAllEntriesHaveTheSameCurrency() {
        when(entryAbsDataSource.query(syncCoordinator.getQueryString(), new String[]{homeCurrency.code}, null))
                .thenReturn(new ArrayList<Entry>());
        syncCoordinator.onDataSourceChanged();
        verifyZeroInteractions(downloader);
    }

    @Test
    public void whenDownloadCompletesTheCoordinatorSavesTheExchangeRates() {
        List<ExchangeRate> exchangeRates = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_1_2000, 1.2439, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_1_2000, 0.9239, -1, 0));

        when(exchangeRateAbsDataSource.bulkInsert(exchangeRates)).thenReturn(exchangeRates);
        syncCoordinator.onDownloadComplete(exchangeRates);
        verify(exchangeRateAbsDataSource).bulkInsert(exchangeRates);
    }

    @Test
    public void ifExRateMissingAfterDownload_CreateAnAttemptedExRate() {
        List<Entry> entriesFromDatabaseWithDifferentCurrency = Arrays.asList(
                new Entry(TEST_GLOBAL_ID, JAN_1_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")),
                new Entry(TEST_GLOBAL_ID, JAN_2_2000, TEST_AMOUNT, TEST_CATEGORY, new Currency("EUR", "€", "Euro")));

        when(entryAbsDataSource.query(syncCoordinator.getQueryString(), new String[]{homeCurrency.code}, null))
                .thenReturn(entriesFromDatabaseWithDifferentCurrency);

        syncCoordinator.onDataSourceChanged();

        //uh oh, the other date is missing...
        List<ExchangeRate> downloadedExRates = new ArrayList<>(Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_1_2000, 0.9345, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, 1.2340, -1, 0)));

        syncCoordinator.onDownloadComplete(downloadedExRates);

        //We expect to see that the number of download attempts goes up for the missing exchange rates
        //And that they get saved with a negative usdRate and a last download attempt timestamp
        List<ExchangeRate> expectedSavedExRates = new ArrayList<>(Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_1_2000, 1.2349, -1, 0),
                new ExchangeRate(-1, "USD", JAN_1_2000, 1.0000, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_2_2000, -1, System.currentTimeMillis(), 1),
                new ExchangeRate(-1, "USD", JAN_2_2000, -1, System.currentTimeMillis(), 1)));

        verify(exchangeRateAbsDataSource).bulkInsert(argThat(containsAllExchangeRates(expectedSavedExRates)));
    }

    //TODO test that rates don't get downloaded again if they already have been!

    //TODO test what happens if a datasource change occurs while waiting for results...

    /**
     * A matcher that takes two lists of Exchange rates and compares them by date (of the form
     * YYYY-MM-DD, currency code, the last download attempt (within the nearest second) and
     * the number of counted download attempts.
     *
     * @param expectedList
     * @return
     */
    public static Matcher<List<ExchangeRate>> containsAllExchangeRates(final List<ExchangeRate> expectedList) {
        return new TypeSafeMatcher<List<ExchangeRate>>() {
            @Override
            protected boolean matchesSafely(List<ExchangeRate> actualList) {
                if (expectedList.size() != actualList.size()) {
                    return false;
                }
                Collections.sort(expectedList, ExchangeRateUtils.comparator());
                Collections.sort(actualList, ExchangeRateUtils.comparator());

                for (int i = 0; i < expectedList.size(); i++) {
                    ExchangeRate expected = expectedList.get(0);
                    ExchangeRate actual = actualList.get(0);

                    long timeDiff = Math.abs(expected.getUtcLastUpdated() - actual.getUtcLastUpdated());

                    if (!expected.equals(actual) ||
                            expected.getDownloadAttempts() != actual.getDownloadAttempts() ||
                            timeDiff > 1000) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("should contain all and only elements of ").appendValue(expectedList);
            }
        };
    }
}