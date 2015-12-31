package org.theronin.expensetracker.data.source;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang.NotImplementedException;
import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.Contract.EntryTable;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.sync.SyncState;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

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

public class DataSourceEntry extends AbsDataSource<Entry> {

    @Inject AbsDataSource<Category> categoryAbsDataSource;
    @Inject AbsDataSource<Currency> currencyAbsDataSource;

    public DataSourceEntry(Context context, InjectedComponent component, DbHelper dbHelper) {
        super(context, dbHelper);
        component.inject(this);
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
        //TODO consider moving the toValues method to this class now
        ContentValues values = entry.toValues();
        checkEntryValues(values);
        return db.insert(EntryTable.TABLE_NAME, null, values);
    }

    @Override
    protected int updateOperation(SQLiteDatabase db, Entry entry) {
        ContentValues values = entry.toValues();
        checkEntryValues(values);
        return db.update(EntryTable.TABLE_NAME, values,
                EntryTable._ID + " = ?",
                new String[]{Long.toString(entry.getId())});
    }

    @Override
    protected int deleteOperation(SQLiteDatabase sb, Collection<Entry> entities) {
        return sb.delete(EntryTable.TABLE_NAME, "_ID IN (" + createEntityIdsArgument(entities) + ")", null);
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

    public int bulkMarkAsDeleted(Collection<Entry> entries) {
        for (Entry entry : entries) {
            entry.setSyncState(SyncState.MARKED_AS_DELETED);
        }
        return bulkUpdate(entries);
    }
}
