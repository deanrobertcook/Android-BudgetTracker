package org.theronin.expensetracker.data.loader;

import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class CurrencyConverter {

    //TODO move in an instance of the DataSourceExchange rate so that this class can control
    //TODO the downloading of exchange rates.
    //Downloading of exchange rates is now also needed by the EntryLoader.

    private final Currency homeCurrency;
    private final List<ExchangeRate> allExchangeRates;
    private final List<ExchangeRate> homeCurrencyRates;

    private List<Long> missingExchangeRateDays;

    public CurrencyConverter(Currency homeCurrency, List<ExchangeRate> allRates) {
        if (homeCurrency == null) {
            throw new IllegalArgumentException("homeCurrency must have a value");
        }
        this.homeCurrency = homeCurrency;
        this.allExchangeRates = allRates;
        this.homeCurrencyRates = findAllExchangeRatesForHomeCurrency(allRates);
    }

    protected List<ExchangeRate> findAllExchangeRatesForHomeCurrency(List<ExchangeRate> allRates) {
        List<ExchangeRate> homeCurrencyRates = new ArrayList<>();
        for (ExchangeRate rate : allRates) {
            if (homeCurrency.code.equals(rate.currencyCode)) {
                homeCurrencyRates.add(rate);
            }
        }
        return homeCurrencyRates;
    }

    public void assignExchangeRatesToEntries(List<Entry> allEntries) {
        missingExchangeRateDays = new ArrayList<>();
        for (Entry entry : allEntries) {
//            Timber.d(entry.toString());
            if (entry.currency.code.equals(homeCurrency.code)) {
//                Timber.d("Entry currency code matches home currency code");
                entry.setDirectExchangeRate(1);
            } else {
//                Timber.d("Entry currency differs from home currency - calculating equivalent " +
//                        "value");

                ExchangeRate foreignExchangeRate = searchExchangeRates(entry, allExchangeRates);
                double directExchangeRate = foreignExchangeRate != null ?
                        calculateDirectExchangeRate(foreignExchangeRate) : -1.0;
                entry.setDirectExchangeRate(directExchangeRate);

                if (foreignExchangeRate == null &&
                        !DateUtils.listContainsDate(missingExchangeRateDays, entry.utcDate)) {
                    missingExchangeRateDays.add(entry.utcDate);
                }
            }
        }
    }

    public List<Long> getMissingExchangeRateDays() {
        return missingExchangeRateDays;
    }

    protected ExchangeRate searchExchangeRates(Entry entry, List<ExchangeRate> exchangeRates) {
        for (ExchangeRate rate : exchangeRates) {
            if (DateUtils.sameDay(entry.utcDate, rate.utcDate) &&
                    entry.currency.code.equals(rate.currencyCode)) {
                return rate;
            }
        }
        return null;
    }

    protected double calculateDirectExchangeRate (ExchangeRate foreignExchangeRate) {
        double homeCurrencyRate = findHomeCurrencyRateForGivenDate(foreignExchangeRate.utcDate);
        return homeCurrencyRate / foreignExchangeRate.usdRate;
    }

    protected double findHomeCurrencyRateForGivenDate(long utcDate) {
        for (ExchangeRate rate : homeCurrencyRates) {
            if (DateUtils.sameDay(rate.utcDate, utcDate)) {
                return rate.usdRate;
            }
        }
        throw new IllegalStateException("There exists an exchange rate for a given entry, but not" +
                " for the home currency on the same day. This shouldn't happen");
    }
}
