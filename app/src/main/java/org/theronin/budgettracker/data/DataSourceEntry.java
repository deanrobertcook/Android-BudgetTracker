package org.theronin.budgettracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.BudgetContract.EntriesView;
import org.theronin.budgettracker.model.Entry;

import java.util.ArrayList;
import java.util.List;

public class DataSourceEntry extends DataSource<Entry> {

    public DataSourceEntry(BudgetTrackerApplication application) {
        super(application);
    }

    @Override
    public long insert(Entry entity) {
        //TODO consider moving the toValues method to this class now
        ContentValues values = entity.toValues();
        checkEntryValues(values);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long entryId = db.insert(BudgetContract.EntriesTable.TABLE_NAME, null, values);
        setDataInValid();
        return entryId;
    }

    private void checkEntryValues(ContentValues values) {
        sanitiseEntryCategoryValues(values);
        sanitiseEntryCurrencyValues(values);
    }

    private void sanitiseEntryCategoryValues(ContentValues values) {
        long categoryId = values.getAsLong(EntriesView.COL_CATEGORY_ID);
        if (categoryId == -1) {
            String categoryName = values.getAsString(EntriesView.COL_CATEGORY_NAME);

            categoryId = application.getDataSourceCategory().getId(categoryName);
            values.put(BudgetContract.EntriesTable.COL_CATEGORY_ID, categoryId);
        }
        values.remove(EntriesView.COL_CATEGORY_NAME);
    }

    private void sanitiseEntryCurrencyValues(ContentValues values) {
        long currencyId = values.getAsLong(EntriesView.COL_CURRENCY_ID);
        if (currencyId == -1) {
            String currencyCode = values.getAsString(EntriesView.COL_CURRENCY_CODE);

            currencyId = application.getDataSourceCurrency().getId(currencyCode);
            values.put(BudgetContract.EntriesTable.COL_CURRENCY_ID, currencyId);
        }
        values.remove(EntriesView.COL_CURRENCY_CODE);
    }

    @Override
    public List<Entry> query(String selection, String[] selectionArgs, String orderBy) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                EntriesView.VIEW_NAME,
                EntriesView.PROJECTION,
                selection,
                selectionArgs,
                null, null, orderBy
        );

        List<Entry> entries = new ArrayList<>();
        while (cursor.moveToNext()) {
            entries.add(Entry.fromCursor(cursor));
        }
        cursor.close();
        setDataValid();
        return entries;
    }
}
