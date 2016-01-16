package org.theronin.expensetracker.data.loader;

import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

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
        boolean missingExchangeRateData = false;
        for (Entry entry : allEntries) {
            if (entry.currency.code.equals(homeCurrency.code)) {
                entry.setHomeAmount(entry.amount);
            } else {
                setEntryHomeAmount(
                        entry,
                        findExchangeRate(entry.utcDate, homeCurrency.code, allExchangeRates),
                        findExchangeRate(entry.utcDate, entry.currency.code, allExchangeRates));
                missingExchangeRateData = (entry.getHomeAmount() == -1);
            }
        }

        if (missingExchangeRateData) {
            callback.needToDownloadExchangeRates();
        }
    }

    private ExchangeRate findExchangeRate(long utcDate, String currencyCode, List<ExchangeRate> exchangeRates) {
        for (ExchangeRate rate : exchangeRates) {
            if (DateUtils.sameDay(utcDate, rate.utcDate) &&
                    currencyCode.equals(rate.currencyCode)) {
                return rate;
            }
        }
        return null;
    }

    /**
     * Calculates what the entry's value would be in the home currency, given the exchange rates for
     * the current currency and the home currency relative to the USD. The exchange rates passed in
     * need to have the same date as the entry itself, and the currentExRate needs to have the same
     * currency as the entry too.
     *
     * @param homeExRate an ExchangeRate representing the exchange rate for the home currency on
     *                   the date this entry was made
     * @param currentExRate an ExchangeRate representing the exchange rate for the currency this entry
     *                      was entered in on the date it was made.
     */
    private void setEntryHomeAmount(Entry entry, ExchangeRate homeExRate, ExchangeRate currentExRate) {
        if (homeExRate == null || currentExRate == null) {
            return; //leave the homeAmount as -1;
        }

        if (!currentExRate.currencyCode.equals(entry.currency.code)) {
            throw new IllegalArgumentException("The current exchange rate needs to have the same " +
                    "currency as the entry itself");
        }

        if (!DateUtils.sameDay(entry.utcDate, homeExRate.utcDate) ||
                !DateUtils.sameDay(entry.utcDate, currentExRate.utcDate)) {
            throw new IllegalArgumentException("The exchange rates needed to calculate this entry's" +
                    " home amount need to be from the same day as the entry");
        }

        double directExchangeRate = homeExRate.getUsdRate() / currentExRate.getUsdRate();

        entry.setHomeAmount(Math.round((double) entry.amount * directExchangeRate));
    }
}
