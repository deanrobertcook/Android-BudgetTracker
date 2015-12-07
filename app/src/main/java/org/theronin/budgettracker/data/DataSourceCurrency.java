package org.theronin.budgettracker.data;

import android.database.Cursor;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.BudgetContract.CurrenciesTable;
import org.theronin.budgettracker.model.Currency;

import java.util.ArrayList;
import java.util.List;

public class DataSourceCurrency extends DataSource<Currency> {

    public DataSourceCurrency(BudgetTrackerApplication application) {
        super(application);
    }

    public long getId(String currencyCode) {
        List<Currency> currencies = query(
                CurrenciesTable.COL_CODE + " = ?",
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
                CurrenciesTable.TABLE_NAME,
                CurrenciesTable.PROJECTION,
                selection,
                selectionArgs,
                null, null, orderBy
        );

        List<Currency> categories = new ArrayList<>();
        while (cursor.moveToNext()) {
            categories.add(Currency.fromCursor(cursor));
        }
        cursor.close();
        setDataValid();
        return categories;
    }
}
