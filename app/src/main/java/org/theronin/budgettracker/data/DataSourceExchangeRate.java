package org.theronin.budgettracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.BudgetContract.ExchangeRateTable;
import org.theronin.budgettracker.model.ExchangeRate;

import java.util.ArrayList;
import java.util.List;

public class DataSourceExchangeRate extends AbsDataSource<ExchangeRate> {

    public DataSourceExchangeRate(BudgetTrackerApplication application) {
        super(application);
    }

    @Override
    public int bulkInsert(List<ExchangeRate> entities) {
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
