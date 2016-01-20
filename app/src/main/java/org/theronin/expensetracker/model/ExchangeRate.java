package org.theronin.expensetracker.model;

import org.theronin.expensetracker.utils.DateUtils;

public class ExchangeRate extends Entity implements Comparable<ExchangeRate> {

    public static final int MAX_DOWNLOAD_ATTEMPTS = 3;

    /**
     * Since currencyCode and date together make the unique key for an ExchangeRate, we don't ever
     * want those to change.
     */
    public final String currencyCode;
    public final long utcDate;
    public final String date;

    private double usdRate;
    private long utcLastUpdated;
    private int downloadAttempts;

    public ExchangeRate(String currencyCode, long utcDate) {
        this(-1, currencyCode, utcDate, -1.0, -1, 0);
    }

    public ExchangeRate(long id,
                        String currencyCode,
                        long utcDate,
                        double usdRate,
                        long utcLastUpdated,
                        int downloadAttempts) {
        this.id = id;
        this.currencyCode = currencyCode;
        this.utcDate = utcDate;
        this.date = DateUtils.getStorageFormattedDate(utcDate);
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

    public double getUsdRate() {
        return usdRate;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExchangeRate)) {
            return false;
        }
        ExchangeRate other = (ExchangeRate) o;
        return currencyCode.equals(other.currencyCode) &&
                DateUtils.sameDay(utcDate, other.utcDate);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(ExchangeRate another) {
        int byDate = -(int) (utcDate - another.utcDate);
        if (byDate == 0) {
            return currencyCode.compareTo(another.currencyCode);
        }
        return byDate;
    }
}
