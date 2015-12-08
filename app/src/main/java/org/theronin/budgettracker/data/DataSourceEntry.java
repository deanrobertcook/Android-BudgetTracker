package org.theronin.budgettracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.BudgetContract.EntryTable;
import org.theronin.budgettracker.data.BudgetContract.EntryView;
import org.theronin.budgettracker.model.Entry;

import java.util.ArrayList;
import java.util.List;

public class DataSourceEntry extends AbsDataSource<Entry> {

    public DataSourceEntry(BudgetTrackerApplication application) {
        super(application);
    }

    @Override
    public long insert(Entry entity) {
        //TODO consider moving the toValues method to this class now
        ContentValues values = entity.toValues();
        checkEntryValues(values);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long entryId = db.insert(EntryTable.TABLE_NAME, null, values);
        setDataInValid();
        return entryId;
    }

    @Override
    public int bulkInsert(List<Entry> entities) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Entry entry : entities) {
                ContentValues values = entry.toValues();
                checkEntryValues(values);
                db.insert(
                        EntryTable.TABLE_NAME,
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

    private void checkEntryValues(ContentValues values) {
        sanitiseEntryCategoryValues(values);
        sanitiseEntryCurrencyValues(values);
    }

    private void sanitiseEntryCategoryValues(ContentValues values) {
        long categoryId = values.getAsLong(EntryView.COL_CATEGORY_ID);
        if (categoryId == -1) {
            String categoryName = values.getAsString(EntryView.COL_CATEGORY_NAME);

            categoryId = application.getDataSourceCategory().getId(categoryName);
            values.put(EntryTable.COL_CATEGORY_ID, categoryId);
        }
        values.remove(EntryView.COL_CATEGORY_NAME);
    }

    private void sanitiseEntryCurrencyValues(ContentValues values) {
        long currencyId = values.getAsLong(EntryView.COL_CURRENCY_ID);
        if (currencyId == -1) {
            String currencyCode = values.getAsString(EntryView.COL_CURRENCY_CODE);

            currencyId = application.getDataSourceCurrency().getId(currencyCode);
            values.put(EntryTable.COL_CURRENCY_ID, currencyId);
        }
        values.remove(EntryView.COL_CURRENCY_CODE);
    }

    @Override
    public boolean delete(Entry entity) {
        int numDeleted = dbHelper.getWritableDatabase().delete(
                EntryTable.TABLE_NAME,
                EntryTable._ID + " = ?",
                new String[]{Long.toString(entity.id)}
        );
        setDataInValid();
        return numDeleted == 1;
    }

    @Override
    public List<Entry> query(String selection, String[] selectionArgs, String orderBy) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                EntryView.VIEW_NAME,
                EntryView.PROJECTION,
                selection,
                selectionArgs,
                null, null, orderBy
        );

        List<Entry> entries = new ArrayList<>();
        while (cursor.moveToNext()) {
            entries.add(Entry.fromCursor(cursor));
        }
        cursor.close();
        return entries;
    }
}
