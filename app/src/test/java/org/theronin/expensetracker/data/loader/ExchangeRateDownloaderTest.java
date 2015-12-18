package org.theronin.expensetracker.data.loader;


import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.theronin.expensetracker.data.loader.ExchangeRateDownloader.API_KEY;
import static org.theronin.expensetracker.data.loader.ExchangeRateDownloader.OPEN_EXCHANGE_URL;

public class ExchangeRateDownloaderTest {

    public static final String JSON_RES_PATH = "app/src/androidTest/res/test_data/%s.json";

    public static final String[] SUPPORTED_CURRENCIES = {
            "USD",
            "AUD",
            "EUR"
    };

    private ExchangeRateDownloader exchangeRateDownloader;

    @Before
    public void setup() {
        exchangeRateDownloader = new ExchangeRateDownloader(SUPPORTED_CURRENCIES);
    }

    @Test
    public void canBuildValidUrlFromUtcTimestamp() throws MalformedURLException {
        long utcDate = 1449597036655L;
        URL expectedUrl = new URL(String.format(OPEN_EXCHANGE_URL, "2015-12-09", API_KEY));

        assertEquals("Produced URL did not match the expected URL", expectedUrl,
                exchangeRateDownloader.buildUrlFromTimestamp(utcDate));
    }

    @Test
    public void jsonDownloadForValidJson() throws MalformedURLException {
        URL url = new File(String.format(JSON_RES_PATH, "2015-12-03")).toURI().toURL();
        String jsonString = exchangeRateDownloader.downloadJson(url);
        System.out.println(jsonString);
        assertTrue("No text was read from a valid file", jsonString.length() > 0);
    }

    @Test
    public void ensureSomeExchangeRatesAreProducedForValidJson() throws MalformedURLException {
        URL url = new File(String.format(JSON_RES_PATH, "2015-12-03")).toURI().toURL();
        String jsonString = exchangeRateDownloader.downloadJson(url);
        List<ExchangeRate> exchangeRates = exchangeRateDownloader.getRatesFromJson(jsonString, 0);
        for (ExchangeRate rate : exchangeRates) {
            System.out.println(rate);
        }
        assertEquals("The number of exchange rates returned doesn't match the size of " +
                "SUPPORTED_CURRENCIES", SUPPORTED_CURRENCIES.length, exchangeRates.size());
    }

    @Test @LargeTest
    public void compareRealDownloadedDataWithLocallySavedData() throws MalformedURLException {
        String downloadDate = "2015-12-03";
        long utcDate = DateUtils.getUtcTimeFromStorageFormattedDate(downloadDate);
        URL url = new File(String.format(JSON_RES_PATH, downloadDate)).toURI().toURL();

        String jsonString = exchangeRateDownloader.downloadJson(url);
        List<ExchangeRate> localExchangeRates = exchangeRateDownloader.getRatesFromJson(jsonString, 0);

        List<ExchangeRate> downloadedExchangeRates = exchangeRateDownloader.downloadExchangeRatesOnDay(utcDate);

        assertEquals("Exchange rate counts differ", localExchangeRates.size(), downloadedExchangeRates.size());
    }

    @Test
    public void fetchUnavailableData_ShouldProduceAllSupportedRates() throws
            MalformedURLException {
        String downloadDate = "2100-12-10";
        URL url = new File(String.format(JSON_RES_PATH, downloadDate)).toURI().toURL();

        String jsonString = exchangeRateDownloader.downloadJson(url);
        List<ExchangeRate> exchangeRates = exchangeRateDownloader.getRatesFromJson(jsonString, 0);

        for (ExchangeRate rate : exchangeRates) {
            System.out.println(rate);
        }

        assertEquals("ExchangeRateDownloader did create blank ExchangeRate objects for all " +
                "supported currencies", SUPPORTED_CURRENCIES.length, exchangeRates.size());
    }
}