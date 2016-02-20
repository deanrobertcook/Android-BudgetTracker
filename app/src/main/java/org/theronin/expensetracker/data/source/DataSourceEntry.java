package org.theronin.expensetracker.data.source;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang.NotImplementedException;
import org.theronin.expensetracker.data.Contract.EntryTable;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;

import java.util.Collection;
import java.util.List;

import timber.log.Timber;

import static org.theronin.expensetracker.data.Contract.EntryView.COL_CURRENCY_CODE;
import static org.theronin.expensetracker.data.Contract.EntryView.COL_CURRENCY_ID;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_AMOUNT;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CATEGORY_ID;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CATEGORY_NAME;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CURRENCY_CODE;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CURRENCY_ID;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_CURRENCY_SYMBOL;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_DATE;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_GLOBAL_ID;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_ID;
import static org.theronin.expensetracker.data.Contract.EntryView.INDEX_SYNC_STATUS;

public class DataSourceEntry extends AbsDataSource<Entry> implements
        AbsDataSource.UpdateListener<Category> {

    AbsDataSource<Category> categoryAbsDataSource;
    AbsDataSource<Currency> currencyAbsDataSource;

    public DataSourceEntry(Context context,
                           DbHelper dbHelper,
                           AbsDataSource<Category> categoryAbsDataSource,
                           AbsDataSource<Currency> currencyAbsDataSource) {
        super(context, dbHelper);
        this.categoryAbsDataSource = categoryAbsDataSource;
        this.categoryAbsDataSource.setListener(this);
        this.currencyAbsDataSource = currencyAbsDataSource;
        Timber.d("Instantiating DataSourceEntry");
    }

    @Override
    protected String getTableName() {
        return EntryView.VIEW_NAME;
    }

    @Override
    protected String[] getQueryProjection() {
        return EntryView.PROJECTION;
    }

    @Override
    protected List<Entry> searchForIdFromEntity(Entry entity) {
        throw new NotImplementedException("Entries can't be searched by any value other than ID");
    }

    @Override
    protected Entry fromCursor(Cursor cursor) {
        long id = cursor.getLong(INDEX_ID);
        String globalId = cursor.getString(INDEX_GLOBAL_ID);
        SyncState syncState = SyncState.valueOf(cursor.getString(INDEX_SYNC_STATUS));
        long utcDateEntered = cursor.getLong(INDEX_DATE);
        long amount = cursor.getLong(INDEX_AMOUNT);

        Category category = new Category(
                cursor.getLong(INDEX_CATEGORY_ID),
                cursor.getString(INDEX_CATEGORY_NAME)
        );

        Currency currency = new Currency(
                cursor.getLong(INDEX_CURRENCY_ID),
                cursor.getString(INDEX_CURRENCY_CODE),
                cursor.getString(INDEX_CURRENCY_SYMBOL)
        );

        return new Entry(id, globalId, syncState, utcDateEntered, amount, category, currency);
    }

    @Override
    protected long insertOperation(SQLiteDatabase db, Entry entry) {
        ContentValues values = getContentValues(entry);
        checkEntryValues(values);
        return db.insertOrThrow(EntryTable.TABLE_NAME, null, values);
    }

    @Override
    protected int updateOperation(SQLiteDatabase db, Entry entry) {
        ContentValues values = getContentValues(entry);
        checkEntryValues(values);
        return db.update(EntryTable.TABLE_NAME, values,
                EntryTable._ID + " = ?",
                new String[]{Long.toString(entry.getId())});
    }

    @Override
    protected int deleteOperation(SQLiteDatabase sb, Collection<Entry> entities) {
        int count = sb.delete(
                EntryTable.TABLE_NAME,
                EntryTable._ID + " IN (" + createEntryIdsInClause(entities) + ")",
                null);

        if (count == 0) {
        //delete failed because the entities have no assigned local ids, use globalIds instead
            count = sb.delete(
                    EntryTable.TABLE_NAME,
                    EntryTable.COL_GLOBAL_ID + " IN (" + createEntryGlobalIdsInClause(entities) + ")",
                    null);
        }

        return count;
    }

    protected String createEntryIdsInClause(Collection<Entry> entries) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Entry entity : entries) {
            checkEntryDeleted(entity);
            sb.append(entity.getId());
            if (i != entries.size() - 1) {
                sb.append(", ");
            }
            i++;
        }
        return sb.toString();
    }

    protected String createEntryGlobalIdsInClause(Collection<Entry> entries) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Entry entity : entries) {
            checkEntryDeleted(entity);
            sb.append(String.format("'%s'", entity.getGlobalId()));
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

    private void checkEntryValues(ContentValues values) {
        changeCategoryObjectToId(values);
        changeCurrencyObjectToId(values);
    }

    private void changeCategoryObjectToId(ContentValues values) {
        long categoryId = values.getAsLong(EntryView.COL_CATEGORY_ID);
        if (categoryId == -1) {
            String categoryName = values.getAsString(EntryView.COL_CATEGORY_NAME);

            categoryId = categoryAbsDataSource.getId(new Category(categoryName));
            values.put(EntryTable.COL_CATEGORY_ID, categoryId);
        }
        values.remove(EntryView.COL_CATEGORY_NAME);
    }

    private void changeCurrencyObjectToId(ContentValues values) {
        long currencyId = values.getAsLong(COL_CURRENCY_ID);
        if (currencyId == -1) {
            String currencyCode = values.getAsString(COL_CURRENCY_CODE);

            currencyId = currencyAbsDataSource.getId(new Currency(currencyCode));
            values.put(EntryTable.COL_CURRENCY_ID, currencyId);
        }
        values.remove(COL_CURRENCY_CODE);
    }

    public int bulkMarkAsDeleted(List<Entry> entries) {
        for (Entry entry : entries) {
            entry.setSyncState(SyncState.MARKED_AS_DELETED);
        }
        return bulkUpdate(entries);
    }

    @Override
    protected ContentValues getContentValues(Entry entry) {
        ContentValues values = new ContentValues();

        if (entry.getId() > -1) {
            values.put(EntryTable._ID, entry.getId());
        }

        values.put(EntryTable.COL_GLOBAL_ID, entry.getGlobalId());
        values.put(EntryTable.COL_SYNC_STATUS, entry.getSyncState().name());

        values.put(EntryTable.COL_DATE, entry.utcDate);
        values.put(EntryTable.COL_AMOUNT, entry.amount);

        values.put(EntryTable.COL_CATEGORY_ID, entry.category.getId());
        values.put(EntryView.COL_CATEGORY_NAME, entry.category.getName());

        values.put(EntryTable.COL_CURRENCY_ID, entry.currency.getId());
        values.put(EntryView.COL_CURRENCY_CODE, entry.currency.code);
        return values;
    }

    @Override
    public void onEntityUpdated(Category category) {
        List<Entry> affectedEntries = query(EntryView.COL_CATEGORY_NAME + " = ?",
                new String[] {category.getName()}, null);

        for (Entry entry : affectedEntries) {
            entry.setSyncState(SyncState.UPDATED);
        }

        bulkUpdate(affectedEntries);
    }
}
