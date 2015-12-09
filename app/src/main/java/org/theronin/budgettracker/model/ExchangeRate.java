package org.theronin.budgettracker.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.theronin.budgettracker.data.BudgetContract.ExchangeRateTable;
import org.theronin.budgettracker.utils.DateUtils;

import static android.provider.BaseColumns._ID;

public class ExchangeRate {
    public final long id;
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

    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        if (id > -1) {
            values.put(_ID, id);
        }
        values.put(ExchangeRateTable.COL_CURRENCY_CODE, currencyCode);
        values.put(ExchangeRateTable.COL_DATE, utcDate);
        values.put(ExchangeRateTable.COL_USD_RATE, usdRate);
        values.put(ExchangeRateTable.COL_LAST_DOWNLOAD_ATTEMPT, utcLastUpdated);
        return values;
    }

    public static ExchangeRate fromCursor(Cursor cursor) {
        return new ExchangeRate(
                cursor.getLong(ExchangeRateTable.INDEX_ID),
                cursor.getString(ExchangeRateTable.INDEX_CURRENCY_CODE),
                cursor.getLong(ExchangeRateTable.INDEX_DATE),
                cursor.getDouble(ExchangeRateTable.INDEX_USD_RATE),
                cursor.getLong(ExchangeRateTable.INDEX_LAST_DOWNLOAD_ATTEMPT)
        );
    }
}
