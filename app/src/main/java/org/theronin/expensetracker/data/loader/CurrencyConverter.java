package org.theronin.expensetracker.data.loader;

import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for finding the exchange rates for the home currency and the current
 * currency of every entry that is passed into it, and then using this to calculate the direct
 * exchange rate (not relative to USD) for each of those entries, figuring out their total value,
 * and then assigning this to the Entry#homeAmount property.
 */
public class CurrencyConverter {
    private final Callback callback;
    private final Currency homeCurrency;

    private List<ExchangeRate> homeCurrencyRates;

    public interface Callback {
        void needToDownloadExchangeRates();
    }

    public CurrencyConverter(Callback callback, Currency homeCurrency) {
        if (homeCurrency == null) {
            throw new IllegalArgumentException("homeCurrency must have a value");
        }
        this.callback = callback;
        this.homeCurrency = homeCurrency;
    }

    public void assignExchangeRatesToEntries(List<ExchangeRate> allExchangeRates, List<Entry> allEntries) {
        this.homeCurrencyRates = findAllExchangeRatesForHomeCurrency(allExchangeRates);

        boolean missingExchangeRateData = false;
        for (Entry entry : allEntries) {
            if (entry.currency.code.equals(homeCurrency.code)) {
                entry.setDirectExchangeRate(1);
            } else {
                ExchangeRate foreignExchangeRate = searchExchangeRates(entry, allExchangeRates);
                double directExchangeRate = foreignExchangeRate != null ? calculateDirectExchangeRate(foreignExchangeRate) : -1.0;
                entry.setDirectExchangeRate(directExchangeRate);

                missingExchangeRateData = foreignExchangeRate == null;
            }
        }

        if (missingExchangeRateData) {
            callback.needToDownloadExchangeRates();
        }
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
        return homeCurrencyRate / foreignExchangeRate.getUsdRate();
    }

    protected double findHomeCurrencyRateForGivenDate(long utcDate) {
        for (ExchangeRate rate : homeCurrencyRates) {
            if (DateUtils.sameDay(rate.utcDate, utcDate)) {
                return rate.getUsdRate();
            }
        }
        throw new IllegalStateException("There exists an exchange rate for a given entry, but not" +
                " for the home currency on the same day. This shouldn't happen");
    }
}
