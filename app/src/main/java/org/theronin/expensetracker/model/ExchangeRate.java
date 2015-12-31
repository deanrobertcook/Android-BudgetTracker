package org.theronin.expensetracker.model;

import org.theronin.expensetracker.utils.DateUtils;

public class ExchangeRate extends Entity {
    public final String currencyCode;
    public final long utcDate;
    public final double usdRate;
    public final long utcLastUpdated;

    public ExchangeRate(String currencyCode, long utcDate, double usd_rate, long utcLastUpdated) {
        this(-1, currencyCode, utcDate, usd_rate, utcLastUpdated);
    }

    public ExchangeRate(long id,
                        String currencyCode,
                        long utcDate,
                        double usdRate,
                        long utcLastUpdated) {
        this.id = id;
        this.currencyCode = currencyCode;
        this.utcDate = utcDate;
        this.usdRate = usdRate;
        this.utcLastUpdated = utcLastUpdated;
    }

    @Override
    public String toString() {
        return String.format(
                "ExchangeRate %s: %f on %s",
                currencyCode, usdRate, DateUtils.getStorageFormattedDate(utcDate)
        );
    }
}
