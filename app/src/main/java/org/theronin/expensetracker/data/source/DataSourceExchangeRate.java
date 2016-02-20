package org.theronin.expensetracker.data.source;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang.NotImplementedException;
import org.theronin.expensetracker.data.Contract.ExchangeRateTable;
import org.theronin.expensetracker.model.ExchangeRate;

import java.util.Collection;
import java.util.List;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;

public class DataSourceExchangeRate extends AbsDataSource<ExchangeRate> {

    public DataSourceExchangeRate(Context context, DbHelper dbHelper) {
        super(context, dbHelper);
        Timber.d("Instantiating DataSourceExchangeRate");
    }

    @Override
    protected String getTableName() {
        return ExchangeRateTable.TABLE_NAME;
    }

    @Override
    protected String[] getQueryProjection() {
        return ExchangeRateTable.PROJECTION;
    }

    @Override
    protected List<ExchangeRate> searchForIdFromEntity(ExchangeRate entity) {
        throw new NotImplementedException("No need to lookup exchange rate IDs");
    }

    @Override
    public ExchangeRate fromCursor(Cursor cursor) {
        return new ExchangeRate(
                cursor.getLong(ExchangeRateTable.INDEX_ID),
                cursor.getString(ExchangeRateTable.INDEX_CURRENCY_CODE),
                cursor.getLong(ExchangeRateTable.INDEX_DATE),
                cursor.getDouble(ExchangeRateTable.INDEX_USD_RATE),
                cursor.getLong(ExchangeRateTable.INDEX_LAST_DOWNLOAD_ATTEMPT),
                cursor.getInt(ExchangeRateTable.INDEX_DOWNLOAD_ATTEMPTS)
        );
    }

    @Override
    protected long insertOperation(SQLiteDatabase db, ExchangeRate exchangeRate) {
        //TODO check that the rates for the given days are not already in the database
        ContentValues values = getContentValues(exchangeRate);
        int replaceAlgorithm = 5;
        return db.insertWithOnConflict(
                ExchangeRateTable.TABLE_NAME,
                null,
                values,
                replaceAlgorithm
        );
    }

    @Override
    public ContentValues getContentValues(ExchangeRate exchangeRate) {
        ContentValues values = new ContentValues();
        if (exchangeRate.getId() > -1) {
            values.put(_ID, exchangeRate.getId());
        }
        values.put(ExchangeRateTable.COL_CURRENCY_CODE, exchangeRate.currencyCode);
        values.put(ExchangeRateTable.COL_DATE, exchangeRate.utcDate);
        values.put(ExchangeRateTable.COL_USD_RATE, exchangeRate.getUsdRate());
        values.put(ExchangeRateTable.COL_LAST_DOWNLOAD_ATTEMPT, exchangeRate.getUtcLastUpdated());
        values.put(ExchangeRateTable.COL_DOWNLOAD_ATTEMPTS, exchangeRate.getDownloadAttempts());
        return values;
    }

    @Override
    protected int updateOperation(SQLiteDatabase db, ExchangeRate entity) {
        throw new NotImplementedException("Updating exchange rates currently doesn't make sense");
    }

    @Override
    protected int deleteOperation(SQLiteDatabase db, List<ExchangeRate> entities) {
        throw new NotImplementedException("Deleting exchange rates currently doesn't make sense");
    }
}
