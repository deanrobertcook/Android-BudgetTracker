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
import java.util.Collection;
import java.util.List;

import timber.log.Timber;

public class DataSourceEntry extends AbsDataSource<Entry> {

    public DataSourceEntry(CustomApplication application) {
        super(application);
    }

    @Override
    protected String getTableName() {
        return EntryView.VIEW_NAME;
    }

    @Override
    public long insert(Entry entry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long entryId = insertOperation(db, entry);
        setDataInValid();
        return entryId;
    }

    private long insertOperation(SQLiteDatabase db, Entry entry) {
        //TODO consider moving the toValues method to this class now
        ContentValues values = entry.toValues();
        checkEntryValues(values);
        return db.insert(EntryTable.TABLE_NAME, null, values);
    }

    @Override
    public int bulkInsert(Collection<Entry> entries) {
        if (entries.size() == 0) {
            return 0;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Entry entry : entries) {
                insertOperation(db, entry);
            }
            db.setTransactionSuccessful();
            setDataInValid();
        } finally {
            db.endTransaction();
        }
        return entries.size();
    }

    @Override
    public boolean update(Entry entry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int affected = updateOperation(db, entry);
        setDataInValid();
        return affected != 0;
    }

    private int updateOperation(SQLiteDatabase db, Entry entry) {
        ContentValues values = entry.toValues();
        checkEntryValues(values);
        return db.update(EntryTable.TABLE_NAME, values,
                EntryTable._ID + " = ?",
                new String[]{Long.toString(entry.id)});
    }

    @Override
    public int bulkUpdate(Collection<Entry> entries) {
        Timber.d("Bulk updating " + entries.size() + " entries");
        if (entries.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Entry entry : entries) {
                updateOperation(db, entry);
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
        changeCurrencyObjectToId(values);
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

    private void changeCurrencyObjectToId(ContentValues values) {
        long currencyId = values.getAsLong(EntryView.COL_CURRENCY_ID);
        if (currencyId == -1) {
            String currencyCode = values.getAsString(EntryView.COL_CURRENCY_CODE);

            currencyId = application.getDataSourceCurrency().getId(currencyCode);
            values.put(EntryTable.COL_CURRENCY_ID, currencyId);
        }
        values.remove(EntryView.COL_CURRENCY_CODE);
    }

    public int bulkMarkAsDeleted(Collection<Entry> entries) {
        for (Entry entry : entries) {
            entry.setSyncState(SyncState.MARKED_AS_DELETED);
        }
        return bulkUpdate(entries);
    }

    @Override
    public boolean delete(Entry entry) {
        List<Entry> entries = new ArrayList<>();
        entries.add(entry);
        int numDeleted = bulkDelete(entries);
        return numDeleted == 1;
    }

    @Override
    public int bulkDelete(Collection<Entry> entries) {
        Timber.d("Bulk deleting " + entries.size() + " entries");
        if (entries.size() == 0) {
            return 0;
        }

        String ids = createEntryIdsArgument(new ArrayList<>(entries));

        int numDeleted = dbHelper.getWritableDatabase().delete(
                EntryTable.TABLE_NAME,
                EntryTable._ID + " IN (" + ids + ")", null
        );
        setDataInValid();
        return numDeleted;
    }

    private String createEntryIdsArgument(Collection<Entry> entries) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Entry entry : entries) {
            checkEntryDeleted(entry);
            sb.append(entry.id);
            if (i != entries.size() - 1) {
                sb.append(", ");
            }
            i++;
        }
        return sb.toString();
    }

    public void checkEntryDeleted(Entry entry) {
        if (entry.getSyncState() != SyncState.DELETE_SYNCED) {
            throw new IllegalStateException("An entry needs to be deleted on the backend (DELETE_SYNCED) before it can" +
                    " be removed from the local database");
        }
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
