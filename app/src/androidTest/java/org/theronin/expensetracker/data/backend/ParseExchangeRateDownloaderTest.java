package org.theronin.expensetracker.data.backend;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.junit.Before;
import org.junit.Test;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.theronin.expensetracker.data.backend.ExchangeRateSyncCoordinatorTest.containsAllExchangeRates;

/**
 * These tests are NOT unit tests. They actually use real world exchange rate data from openexchangerate.org
 * and query the backend for exchange rates a few given days, which I have carefully picked out in order
 * to ensure my downloaders are working properly. This also partly tests the "exchangeRate" cloud code
 * function, since we delete previously downloaded test data and force the backend to download rates for
 * the given test days.
 */
public class ParseExchangeRateDownloaderTest {

    public static final String PARSE_DATE_KEY = "date";

    public static final long JAN_1_2000 = DateUtils.getUtcTime("2000-01-01");
    public static final long FEB_1_2000 = DateUtils.getUtcTime("2000-02-01");

    /**
     * These rates have been taken manually from openexchangerate.org.
     */
    public static final double EUR_JAN_1_2000 = 0.993161;
    public static final double AUD_JAN_1_2000 = 1.532914;
    public static final double AUD_FEB_1_2000 = 1.579927;

    public static final int QUERY_LIMIT = 200;
    public static final int COUNT_RATES_JAN_1_2000 = 43;
    public static final int COUNT_RATES_FEB_1_2000 = 59;

    private ExchangeRateDownloader.Callback mockCallback;
    private ParseExchangeRateDownloader downloader;


    @Before
    public void setup() {
        mockCallback = mock(ExchangeRateDownloader.Callback.class);
        downloader = new ParseExchangeRateDownloader();
        downloader.setCallback(mockCallback);
    }

    /**
     * Removes the downloaded exchange rates for the given test days from the backend. This forces
     * the backend to actually query openexchangerate.org for the given days.
     * @throws ParseException
     */
    @Before
    public void removeExchangeRatesFromBackend() throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<>("ExchangeRate");
        query.setLimit(QUERY_LIMIT);
        List<String> onGivenDates =  Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000),
                DateUtils.getStorageFormattedDate(FEB_1_2000)
        );

        query.whereContainedIn(PARSE_DATE_KEY, onGivenDates);
        List<ParseObject> objects = query.find();

        ParseObject.deleteAll(objects);

        assertNumberOfEntriesOnBackend(0, onGivenDates);
    }

    @Test
    public void noDuplicateRatesAreDownloadedForSameDate() throws ParseException {
        Set<String> datesToDownload = new HashSet<>(Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000)
        ));

        Set<String> codesToDownload = new HashSet<>(Arrays.asList(
                "EUR",
                "AUD"
        ));

        downloader.downloadExchangeRates(datesToDownload, codesToDownload);

        List<ExchangeRate> expectedRates = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_1_2000, AUD_JAN_1_2000, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_1_2000, EUR_JAN_1_2000, -1, 0));

        verify(mockCallback).onDownloadComplete(containsAllExchangeRates(expectedRates));

        assertNumberOfEntriesOnBackend(COUNT_RATES_JAN_1_2000, Arrays.asList(DateUtils.getStorageFormattedDate(JAN_1_2000)));
    }

    @Test
    public void noDuplicatesDownloadedForSameCode() throws ParseException {
        Set<String> datesToDownload = new HashSet<>(Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000),
                DateUtils.getStorageFormattedDate(FEB_1_2000)
        ));

        Set<String> codesToDownload = new HashSet<>(Arrays.asList(
                "AUD"
        ));

        downloader.downloadExchangeRates(datesToDownload, codesToDownload);

        List<ExchangeRate> expectedRates = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_1_2000, AUD_JAN_1_2000, -1, 0),
                new ExchangeRate(-1, "AUD", FEB_1_2000, AUD_FEB_1_2000, -1, 0));

        verify(mockCallback).onDownloadComplete(containsAllExchangeRates(expectedRates));

        assertNumberOfEntriesOnBackend(COUNT_RATES_FEB_1_2000 + COUNT_RATES_JAN_1_2000, Arrays.asList(
                DateUtils.getStorageFormattedDate(JAN_1_2000),
                DateUtils.getStorageFormattedDate(FEB_1_2000)
        ));
    }

    private void assertNumberOfEntriesOnBackend(int expected, List<String> onGivenDates) throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<>("ExchangeRate");
        query.setLimit(QUERY_LIMIT);
        query.whereContainedIn(PARSE_DATE_KEY, onGivenDates);
        List<ParseObject> objects = query.find();
        assertEquals("Number of saved ExchangeRates is different from expected", expected, objects.size());
    }
}