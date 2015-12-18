package org.theronin.expensetracker.data;

import android.database.Cursor;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.data.Contract.CurrencyTable;
import org.theronin.expensetracker.model.Currency;

import java.util.ArrayList;
import java.util.List;

public class DataSourceCurrency extends AbsDataSource<Currency> {

    public DataSourceCurrency(CustomApplication application) {
        super(application);
    }

    public long getId(String currencyCode) {
        List<Currency> currencies = query(
                CurrencyTable.COL_CODE + " = ?",
                new String[]{currencyCode},
                null
        );

        if (currencies.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("The currency %s is not supported", currencyCode)
            );
        }
        return currencies.get(0).id;
    }

    @Override
    public List<Currency> query(String selection, String[] selectionArgs, String orderBy) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                CurrencyTable.TABLE_NAME,
                CurrencyTable.PROJECTION,
                selection,
                selectionArgs,
                null, null, orderBy
        );

        List<Currency> categories = new ArrayList<>();
        while (cursor.moveToNext()) {
            categories.add(Currency.fromCursor(cursor));
        }
        cursor.close();
        return categories;
    }
}
