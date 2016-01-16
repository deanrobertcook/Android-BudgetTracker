package org.theronin.expensetracker.data.loader;

import org.junit.Before;
import org.junit.Test;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class CurrencyConverterTest {

    private static final long DEC_03_2015 = DateUtils.getUtcTime("2015-12-03");
    private static final long DEC_04_2015 = DateUtils.getUtcTime("2015-12-04");

    private CurrencyConverter.Callback mockCallback;
    private CurrencyConverter converter;

    @Before
    public void setup() {
        mockCallback = mock(CurrencyConverter.Callback.class);
        converter = new CurrencyConverter(mockCallback, new Currency("AUD"));
    }

    @Test
    public void entriesWithSameCurrencyAsHome_ShouldBeAssignedRateOfOne() {
        List<Entry> entries = Arrays.asList(
                new Entry(DEC_03_2015, 10, null, new Currency("AUD")),
                new Entry(DEC_04_2015, 10, null, new Currency("AUD"))
        );

        //We shouldn't need to pass in any exchange rates
        converter.assignExchangeRatesToEntries(new ArrayList<ExchangeRate>(), entries);

        for (Entry entry : entries) {
            assertEquals("Entry does not have an exchange rate of 1",
                    1.0, entry.getDirectExchangeRate());
        }
    }

    @Test
    public void entryWithDifferentCurrency_ShouldBeAssignedRateOfGreaterThanZero() {
        int entryAmount = 100;
        List<Entry> entries = Arrays.asList(
                new Entry(DEC_03_2015, entryAmount, null, new Currency("EUR"))
        );

        double currentRate = 2.00;
        double homeRate = 3.00;

        List<ExchangeRate> rates = Arrays.asList(
                new ExchangeRate(-1, "EUR", DEC_03_2015, currentRate, -1, -1),
                new ExchangeRate(-1, "AUD", DEC_03_2015, homeRate, -1, -1)
        );

        converter.assignExchangeRatesToEntries(rates, entries);

        assertEquals("Rate for entry not correct", homeRate / currentRate, entries.get(0).getDirectExchangeRate());
    }

    @Test
    public void entryWithDifferentCurrency_AndNoExchangeData_ShouldBeAssignedRateOfNegativeOne() {
        int entryAmount = 100;
        List<Entry> entries = Arrays.asList(
                new Entry(DEC_03_2015, entryAmount, null, new Currency("EUR"))
        );

        converter.assignExchangeRatesToEntries(new ArrayList<ExchangeRate>(), entries);

        assertEquals("Rate for entry not correct", -1.0, entries.get(0).getDirectExchangeRate());
    }

    @Test
    public void anyMissingExchangeRateDataShouldTriggerCallback() {
        int entryAmount = 100;
        List<Entry> entries = Arrays.asList(
                new Entry(DEC_03_2015, entryAmount, null, new Currency("EUR"))
        );

        converter.assignExchangeRatesToEntries(new ArrayList<ExchangeRate>(), entries);

        verify(mockCallback).needToDownloadExchangeRates();
    }
}