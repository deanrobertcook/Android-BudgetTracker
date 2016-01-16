package org.theronin.expensetracker.data.loader;

import junit.framework.Assert;

import org.junit.Test;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.theronin.expensetracker.data.loader.ExchangeRateDownloaderTest.SUPPORTED_CURRENCIES;
import static org.theronin.expensetracker.data.loader.ExchangeRateDownloaderTest.JSON_RES_PATH;


public class CurrencyConverterTest {

    private final CurrencyConverter CONVERTER;

    private final Currency HOME_CURRENCY = new Currency("EUR", "€");
    private final String[] TEST_DATA_DATES = {
            "2015-12-03",
            "2015-12-06",
            "2015-12-07",
            "2015-12-08"
    };

    public CurrencyConverterTest() throws MalformedURLException {
        List<ExchangeRate> testRates = new ArrayList<>();
        for (String date : TEST_DATA_DATES) {
            List<ExchangeRate> daysRates = findTestRatesForDate(date);
            testRates.addAll(daysRates);
        }

        CONVERTER = new CurrencyConverter(HOME_CURRENCY, testRates);
    }

    private List<ExchangeRate> findTestRatesForDate(String date) throws MalformedURLException {
        long utcDate = DateUtils.getUtcTime(date);
        URL url = new File(String.format(JSON_RES_PATH, date)).toURI().toURL();
        ExchangeRateDownloader exchangeRateDownloader = new ExchangeRateDownloader(SUPPORTED_CURRENCIES);
        String jsonString = exchangeRateDownloader.downloadJson(url);
        return exchangeRateDownloader.getRatesFromJson(jsonString,
                utcDate);
    }

    @Test
    public void entriesWithSameCurrencyAsHome_ShouldBeAssignedRateOfOne() {
        List<Entry> entries = new ArrayList<>();

        entries.add(new Entry(DateUtils.getUtcTime("2015-12-03"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTime("2015-12-06"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTime("2015-12-07"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTime("2015-12-08"),
                1000, new Category(""), HOME_CURRENCY));

        CONVERTER.assignExchangeRatesToEntries(entries);

        assertEquals("Should be no missing exchange rate data", 0,
                CONVERTER.getMissingExchangeRateDays().size());

        for (Entry entry : entries) {
            assertEquals("Entry does not have an exchange rate of 1",
                    1.0, entry.getDirectExchangeRate());
        }
    }

    @Test
    public void entryWithNoExchangeData_ButSameCurrency_ShouldBeAssignedRateOfOne() {
        List<Entry> entries = new ArrayList<>();

        entries.add(new Entry(DateUtils.getUtcTime("2015-11-06"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTime("2015-11-03"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTime("2014-12-01"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTime("2014-12-08"),
                1000, new Category(""), HOME_CURRENCY));

        CONVERTER.assignExchangeRatesToEntries(entries);

        assertEquals("Should be no missing exchange rate data", 0,
                CONVERTER.getMissingExchangeRateDays().size());

        for (Entry entry : entries) {
            assertEquals("Entry does not have an exchange rate of 1",
                    1.0, entry.getDirectExchangeRate());
        }
    }

    @Test
    public void entryWithDifferentCurrency_ShouldBeAssignedRateOfGreaterThanZero() {
        List<Entry> entries = new ArrayList<>();

        Currency currency = new Currency("AUD", "$");

        entries.add(new Entry(DateUtils.getUtcTime("2015-12-03"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTime("2015-12-06"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTime("2015-12-07"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTime("2015-12-08"),
                1000, new Category(""), currency));

        CONVERTER.assignExchangeRatesToEntries(entries);

        assertEquals("Should be no missing exchange rate data", 0,
                CONVERTER.getMissingExchangeRateDays().size());

        for (Entry entry : entries) {
            Assert.assertTrue("Entry does not have an exchange rate of >0",
                    entry.getDirectExchangeRate() > 0);
        }
    }

    @Test
    public void entryWithDifferentCurrency_AndNoExchangeData_ShouldBeAssignedRateOfNegativeOne() {
        List<Entry> entries = new ArrayList<>();

        Currency currency = new Currency("AUD", "$");

        entries.add(new Entry(DateUtils.getUtcTime("2015-11-06"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTime("2015-11-03"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTime("2014-12-01"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTime("2014-12-08"),
                1000, new Category(""), currency));

        CONVERTER.assignExchangeRatesToEntries(entries);

        assertEquals("All exchange rate data should be missing", entries.size(),
                CONVERTER.getMissingExchangeRateDays().size());

        for (Entry entry : entries) {
            assertEquals("Entry does not have an exchange rate of -1",
                    -1.0, entry.getDirectExchangeRate());
        }
    }

    @Test
    public void missingExchangeRateDays_ShouldOnlyIncrement_IfExchangeRateNotInDatabase() {
        String missingExchangeDataDay = "2015-12-11";
        String notMissingExchangeDataDay = "2015-12-10";

        List<ExchangeRate> exchangeRates = new ArrayList<>();
        exchangeRates.add(new ExchangeRate("AUD",
                DateUtils.getUtcTime(notMissingExchangeDataDay),
                -1.00,
                System.currentTimeMillis()));
        //Need to ad an exchange rate for the home currency on the same day
        exchangeRates.add(new ExchangeRate("EUR",
                DateUtils.getUtcTime(notMissingExchangeDataDay),
                -1.00,
                System.currentTimeMillis()));

        List<Entry> entries = new ArrayList<>();
        //Even though the exchange rate for this date has a negative (not available) rate,
        //We don't want to mark it as missing, as we've attempted to download it before.
        entries.add(new Entry(
                DateUtils.getUtcTime(notMissingExchangeDataDay),
                100,
                new Category("test"),
                new Currency("AUD", "$")
        ));
        //This entry should trigger an increment to missing entries, since there is no exchange rate
        //at all for this date, even one with a negative (na) exchange rate
        entries.add(new Entry(
                DateUtils.getUtcTime(missingExchangeDataDay),
                100,
                new Category("test"),
                new Currency("AUD", "$")
        ));

        //Home currency needs to be different to trigger checking of exchange rate data.
        CurrencyConverter currencyConverter = new CurrencyConverter(HOME_CURRENCY, exchangeRates);
        currencyConverter.assignExchangeRatesToEntries(entries);

        assertEquals("Missing entries is not as expected", 1,
                currencyConverter.getMissingExchangeRateDays().size());
        assertEquals("The missing entry day is not as expected", missingExchangeDataDay,
                DateUtils.getStorageFormattedDate(
                        currencyConverter.getMissingExchangeRateDays().get(0)));
    }

}