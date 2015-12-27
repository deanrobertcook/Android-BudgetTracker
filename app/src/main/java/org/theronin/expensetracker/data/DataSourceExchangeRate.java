package org.theronin.expensetracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang.NotImplementedException;
import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.data.Contract.ExchangeRateTable;
import org.theronin.expensetracker.model.ExchangeRate;

import java.util.Collection;

public class DataSourceExchangeRate extends AbsDataSource<ExchangeRate> {

    public DataSourceExchangeRate(CustomApplication application) {
        super(application);
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
    public ExchangeRate fromCursor(Cursor cursor) {
        return new ExchangeRate(
                cursor.getLong(ExchangeRateTable.INDEX_ID),
                cursor.getString(ExchangeRateTable.INDEX_CURRENCY_CODE),
                cursor.getLong(ExchangeRateTable.INDEX_DATE),
                cursor.getDouble(ExchangeRateTable.INDEX_USD_RATE),
                cursor.getLong(ExchangeRateTable.INDEX_LAST_DOWNLOAD_ATTEMPT)
        );
    }

    @Override
    protected long insertOperation(SQLiteDatabase db, ExchangeRate exchangeRate) {
        //TODO check that the rates for the given days are not already in the database
        ContentValues values = exchangeRate.toValues();
        return db.insert(
                ExchangeRateTable.TABLE_NAME,
                null,
                values
        );
    }

    @Override
    protected int updateOperation(SQLiteDatabase db, ExchangeRate entity) {
        throw new NotImplementedException("Updating exchange rates currently doesn't make sense");
    }

    @Override
    protected int deleteOperation(SQLiteDatabase sb, Collection<ExchangeRate> entities) {
        throw new NotImplementedException("Deleting exchange rates currently doesn't make sense");
    }
}
