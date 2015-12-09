package org.theronin.budgettracker.data.loader;

import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CurrencyConverter {

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
            Timber.d(entry.toString());
            if (entry.currency.code.equals(homeCurrency.code)) {
                Timber.d("Entry currency code matches home currency code");
                entry.setDirectExchangeRate(1);
            } else {
                Timber.d("Entry currency differs from home currency - calculating equivalent " +
                        "value");
                entry.setDirectExchangeRate(findDirectExchangeRateForEntry(entry, allExchangeRates));
            }
            if (entry.getDirectExchangeRate() == -1.0) {
                addDateToMissingExchangeRateDays(entry.utcDate);
            }
        }
    }

    protected void addDateToMissingExchangeRateDays(long utcDateToEnter) {
        boolean alreadyContained = false;
        for(Long utcDate: missingExchangeRateDays) {
            if (DateUtils.sameDay(utcDate, utcDateToEnter)) {
                alreadyContained = true;
            }
        }
        if (!alreadyContained) {
            missingExchangeRateDays.add(utcDateToEnter);
        }
    }

    public List<Long> getMissingExchangeRateDays() {
        return missingExchangeRateDays;
    }

    protected double findDirectExchangeRateForEntry(Entry entry, List<ExchangeRate> allExchangeRates) {
        ExchangeRate exchangeRate = searchExchangeRates(entry, allExchangeRates);
        if (exchangeRate != null) {
            return calculateDirectExchangeRate(exchangeRate);
        }
        return -1;
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
