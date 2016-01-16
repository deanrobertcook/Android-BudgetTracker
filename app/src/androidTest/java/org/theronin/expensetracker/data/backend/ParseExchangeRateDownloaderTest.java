package org.theronin.expensetracker.data.backend;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.junit.Before;
import org.junit.Test;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.util.Arrays;
import java.util.List;

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

    public static final long JAN_1_2000 = DateUtils.getUtcTimeFromStorageFormattedDate("2000-01-01");
    public static final long FEB_1_2000 = DateUtils.getUtcTimeFromStorageFormattedDate("2000-02-01");

    /**
     * These rates have been taken manually from openexchangerate.org.
     */
    public static final double EUR_JAN_1_2000 = 0.993161;
    public static final double AUD_JAN_1_2000 = 1.532914;
    public static final double AUD_FEB_1_2000 = 1.579927;

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
     * the backend to actually query openexchangerate.org for the given test days.
     * @throws ParseException
     */
    @Before
    public void removeExchangeRatesFromBackend() throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<>("ExchangeRate");
        query.whereContains(PARSE_DATE_KEY, DateUtils.getStorageFormattedDate(JAN_1_2000));
        query.whereContains(PARSE_DATE_KEY, DateUtils.getStorageFormattedDate(FEB_1_2000));
        List<ParseObject> objects = query.find();

        ParseObject.deleteAll(objects);
    }

    @Test
    public void noDuplicateRatesAreDownloadedForSameDate(){
        List<ExchangeRate> ratesToDownload = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0));

        downloader.downloadExchangeRates(ratesToDownload);

        List<ExchangeRate> expectedRates = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_1_2000, AUD_JAN_1_2000, -1, 0),
                new ExchangeRate(-1, "EUR", JAN_1_2000, EUR_JAN_1_2000, -1, 0));

        verify(mockCallback).onDownloadComplete(containsAllExchangeRates(expectedRates));
    }

    @Test
    public void noDuplicatesDownloadedForSameCode() {
        List<ExchangeRate> ratesToDownload = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", FEB_1_2000, -1, -1, 0));

        downloader.downloadExchangeRates(ratesToDownload);

        List<ExchangeRate> expectedRates = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_1_2000, AUD_JAN_1_2000, -1, 0),
                new ExchangeRate(-1, "AUD", FEB_1_2000, AUD_FEB_1_2000, -1, 0));

        verify(mockCallback).onDownloadComplete(containsAllExchangeRates(expectedRates));
    }
}