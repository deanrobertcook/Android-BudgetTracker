package org.theronin.expensetracker.model;

import org.theronin.expensetracker.utils.DateUtils;

public class ExchangeRate extends Entity {

    public static final int MAX_DOWNLOAD_ATTEMPTS = 3;

    public final String currencyCode;
    public final long utcDate;

    private double usdRate;
    private long utcLastUpdated;
    private int downloadAttempts;

    public ExchangeRate(long id,
                        String currencyCode,
                        long utcDate,
                        double usdRate,
                        long utcLastUpdated,
                        int downloadAttempts) {
        this.id = id;
        this.currencyCode = currencyCode;
        this.utcDate = utcDate;
        this.usdRate = usdRate;
        this.utcLastUpdated = utcLastUpdated;
        this.downloadAttempts = downloadAttempts;
    }

    public void onDownloadFailed() {
        this.utcLastUpdated = System.currentTimeMillis();
        if (downloadAttempts > MAX_DOWNLOAD_ATTEMPTS) {
            throw new IllegalStateException("We shouldn't attempt to download this exchange rate again");
        }
        this.downloadAttempts++;
    }

    public long getUtcLastUpdated() {
        return utcLastUpdated;
    }

    public int getDownloadAttempts() {
        return downloadAttempts;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(
                "currency: %s, usdRate: %f, date: %s, lastUpdated: %s, downloadAttempts: %d",
                currencyCode, usdRate, DateUtils.getStorageFormattedDate(utcDate),
                DateUtils.getStorageFormattedDate(utcLastUpdated), downloadAttempts
        );
    }

    public boolean equals(ExchangeRate other) {
        if (other == null) {
            return false;
        }
        String thisDate = DateUtils.getDisplayFormattedDate(this.utcDate);
        String otherDate = DateUtils.getDisplayFormattedDate(other.utcDate);
        return this.currencyCode.equals(other.currencyCode) && thisDate.equals(otherDate);
    }

    public void setUsdRate(double usdRate) {
        this.usdRate = usdRate;
    }

    public double getUsdRate() {
        return usdRate;
    }
}
