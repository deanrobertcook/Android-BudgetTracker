package org.theronin.expensetracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.data.Contract.ExchangeRateTable;
import org.theronin.expensetracker.model.ExchangeRate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataSourceExchangeRate extends AbsDataSource<ExchangeRate> {

    public DataSourceExchangeRate(CustomApplication application) {
        super(application);
    }

    @Override
    protected String getTableName() {
        return ExchangeRateTable.TABLE_NAME;
    }

    @Override
    public int bulkInsert(Collection<ExchangeRate> entities) {
        //TODO check that the rates for the given days are not already in the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ExchangeRate exchangeRate : entities) {
                ContentValues values = exchangeRate.toValues();
                db.insert(
                        ExchangeRateTable.TABLE_NAME,
                        null,
                        values
                );
            }
            db.setTransactionSuccessful();
            setDataInValid();
        } finally {
            db.endTransaction();
        }
        return entities.size();
    }

    @Override
    public List<ExchangeRate> query(String selection, String[] selectionArgs, String orderBy) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                ExchangeRateTable.TABLE_NAME,
                ExchangeRateTable.PROJECTION,
                selection,
                selectionArgs,
                null, null, orderBy
        );

        List<ExchangeRate> exchangeRates = new ArrayList<>();
        while (cursor.moveToNext()) {
            exchangeRates.add(ExchangeRate.fromCursor(cursor));
        }
        cursor.close();
        return exchangeRates;
    }
}
