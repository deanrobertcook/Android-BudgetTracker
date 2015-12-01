package org.theronin.budgettracker.model;

public class ExchangeRate {
    public final long id;
    public final String currencyCode;
    public final long utcDate;
    public final double usd_rate;

    public ExchangeRate(long id, String currencyCode, long utcDate, double usd_rate) {
        this.id = id;
        this.currencyCode = currencyCode;
        this.utcDate = utcDate;
        this.usd_rate = usd_rate;
    }
}
