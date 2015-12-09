package org.theronin.budgettracker.data.loader;

import junit.framework.Assert;

import org.junit.Test;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.utils.DateUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class CurrencyConverterTest {

    private static final String JSON_RES_PATH = "app/src/androidTest/res/test_data/%s.json";

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
        long utcDate = DateUtils.getUtcTimeFromStorageFormattedDate(date);
        URL url = new File(String.format(JSON_RES_PATH, date)).toURI().toURL();
        ExchangeRateDownloader exchangeRateDownloader = new ExchangeRateDownloader();
        String jsonString = exchangeRateDownloader.downloadJson(url);
        List<ExchangeRate> exchangeRates = exchangeRateDownloader.getRatesFromJson(jsonString,
                utcDate);
        return exchangeRates;
    }

    @Test
    public void entriesWithSameCurrencyAsHome_ShouldBeAssignedRateOfOne() {
        List<Entry> entries = new ArrayList<>();

        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-12-03"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-12-06"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-12-07"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-12-08"),
                1000, new Category(""), HOME_CURRENCY));

        CONVERTER.assignExchangeRatesToEntries(entries);

        Assert.assertEquals("Should be no missing exchange rate data", 0,
                CONVERTER.getMissingExchangeRateDays());

        for (Entry entry : entries) {
            Assert.assertEquals("Entry does not have an exchange rate of 1",
                    1.0, entry.getDirectExchangeRate());
        }
    }

    @Test
    public void entryWithNoExchangeData_ButSameCurrency_ShouldBeAssignedRateOfOne() {
        List<Entry> entries = new ArrayList<>();

        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-11-06"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-11-03"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2014-12-01"),
                1000, new Category(""), HOME_CURRENCY));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2014-12-08"),
                1000, new Category(""), HOME_CURRENCY));

        CONVERTER.assignExchangeRatesToEntries(entries);

        Assert.assertEquals("Should be no missing exchange rate data", 0,
                CONVERTER.getMissingExchangeRateDays());

        for (Entry entry : entries) {
            Assert.assertEquals("Entry does not have an exchange rate of 1",
                    1.0, entry.getDirectExchangeRate());
        }
    }

    @Test
    public void entryWithDifferentCurrency_ShouldBeAssignedRateOfGreaterThanZero() {
        List<Entry> entries = new ArrayList<>();

        Currency currency = new Currency("AUD", "$");

        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-12-03"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-12-06"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-12-07"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-12-08"),
                1000, new Category(""), currency));

        CONVERTER.assignExchangeRatesToEntries(entries);

        Assert.assertEquals("Should be no missing exchange rate data", 0,
                CONVERTER.getMissingExchangeRateDays());

        for (Entry entry : entries) {
            Assert.assertTrue("Entry does not have an exchange rate of >0",
                    entry.getDirectExchangeRate() > 0);
        }
    }

    @Test
    public void entryWithDifferentCurrency_AndNoExchangeData_ShouldBeAssignedRateOfNegativeOne() {
        List<Entry> entries = new ArrayList<>();

        Currency currency = new Currency("AUD", "$");

        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-11-06"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2015-11-03"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2014-12-01"),
                1000, new Category(""), currency));
        entries.add(new Entry(DateUtils.getUtcTimeFromStorageFormattedDate("2014-12-08"),
                1000, new Category(""), currency));

        CONVERTER.assignExchangeRatesToEntries(entries);

        Assert.assertEquals("All exchange rate data should be missing", entries.size(),
                CONVERTER.getMissingExchangeRateDays());

        for (Entry entry : entries) {
            Assert.assertEquals("Entry does not have an exchange rate of >0",
                    -1.0, entry.getDirectExchangeRate());
        }
    }

}