package org.theronin.expensetracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.data.Contract.EntryTable;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.sync.SyncState;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
import java.util.List;

public class DataSourceEntry extends AbsDataSource<Entry> {

    public DataSourceEntry(CustomApplication application) {
        super(application);
    }

    @Override
    public long insert(Entry entry) {
        //TODO consider moving the toValues method to this class now
        ContentValues values = entry.toValues();
        checkEntryValues(values);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long entryId = db.insert(EntryTable.TABLE_NAME, null, values);
        setDataInValid();
        return entryId;
    }

    @Override
    public boolean update(Entry entry) {
        ContentValues values = entry.toValues();
        checkEntryValues(values);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int affected = db.update(EntryTable.TABLE_NAME, values,
                EntryTable._ID + " = ?",
                new String[]{Long.toString(entry.id)});

        setDataInValid();
        return affected != 0;
    }

    @Override
    public int bulkInsert(List<Entry> entries) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Entry entry : entries) {
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
        return entries.size();
    }

    private void checkEntryValues(ContentValues values) {
        changeCategoryObjectToId(values);
        changeCurrencyObejctToId(values);
    }

    private void changeCategoryObjectToId(ContentValues values) {
        long categoryId = values.getAsLong(EntryView.COL_CATEGORY_ID);
        if (categoryId == -1) {
            String categoryName = values.getAsString(EntryView.COL_CATEGORY_NAME);

            categoryId = application.getDataSourceCategory().getId(categoryName);
            values.put(EntryTable.COL_CATEGORY_ID, categoryId);
        }
        values.remove(EntryView.COL_CATEGORY_NAME);
    }

    private void changeCurrencyObejctToId(ContentValues values) {
        long currencyId = values.getAsLong(EntryView.COL_CURRENCY_ID);
        if (currencyId == -1) {
            String currencyCode = values.getAsString(EntryView.COL_CURRENCY_CODE);

            currencyId = application.getDataSourceCurrency().getId(currencyCode);
            values.put(EntryTable.COL_CURRENCY_ID, currencyId);
        }
        values.remove(EntryView.COL_CURRENCY_CODE);
    }

    public boolean markAsDeleted(Entry entry) {
        Entry updatedEntry = new Entry(
                entry.id, entry.globalId, SyncState.DELETED,
                entry.utcDate, entry.amount, entry.category, entry.currency
        );
        return update(updatedEntry);
    }

    @Override
    public boolean delete(Entry entry) {
        if (entry.syncState != SyncState.DELETED) {
            throw new IllegalStateException("To delete an entry, it needs to be marked as deleted first");
        }
        int numDeleted = dbHelper.getWritableDatabase().delete(
                EntryTable.TABLE_NAME,
                EntryTable._ID + " = ?",
                new String[]{Long.toString(entry.id)}
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
