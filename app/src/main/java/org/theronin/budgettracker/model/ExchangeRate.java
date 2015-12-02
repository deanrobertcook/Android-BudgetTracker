package org.theronin.budgettracker.model;

import org.theronin.budgettracker.utils.DateUtils;

public class ExchangeRate {
    public final long id;
    public final String currencyCode;
    public final long utcDate;
    public final double usd_rate;

    public ExchangeRate(String currencyCode, long utcDate, double usd_rate) {
        this(-1, currencyCode, utcDate, usd_rate);
    }

    public ExchangeRate(long id, String currencyCode, long utcDate, double usd_rate) {
        this.id = id;
        this.currencyCode = currencyCode;
        this.utcDate = utcDate;
        this.usd_rate = usd_rate;
    }

    @Override
    public String toString() {
        return String.format(
                "ExchangeRate %s: %f on %s",
                currencyCode, usd_rate, DateUtils.getStorageFormattedDate(utcDate)
        );
    }
}
