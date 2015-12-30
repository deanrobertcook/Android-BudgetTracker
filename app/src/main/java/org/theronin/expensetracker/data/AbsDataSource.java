package org.theronin.expensetracker.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.sync.SyncState;
import org.theronin.expensetracker.model.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public abstract class AbsDataSource<T extends Entity> {

    public static final String ACCOUNT = "dummyaccount";

    protected Context context;
    protected DbHelper dbHelper;

    private Set<Observer> observers;

    public AbsDataSource(Context context, DbHelper dbHelper) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.observers = new HashSet<>();
    }

    public interface Observer {
        void onDataSourceChanged();
    }

    public void registerObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("DataSource.Observers cannot be null");
        }
        this.observers.add(observer);
    }

    public void unregisterObserver(Observer observer) {
        observers.remove(observer);
//        if (!observers.remove(observer)) {
//            throw new IllegalStateException("The observer is not registered to this DataSource");
//        }
    }

    /**
     * This method should be called after an insert, delete, or update method is called on the data
     * source, to signal any observers that the underlying data source will now be out of date
     */
    public void setDataInValid() {
        for (Observer observer : observers) {
            observer.onDataSourceChanged();
        }
        requestSync();
        Timber.d(this.getClass().toString() + " data set as invalid");
    }

    public void requestSync() {
        Bundle extras = new Bundle();
        ContentResolver.requestSync(createSyncAccount(), getContentAuthority(), extras);
    }

    private Account createSyncAccount() {
        Account account = new Account(ACCOUNT, getAccountType());
        AccountManager accountManager = AccountManager.get(context);

        ContentResolver.setSyncAutomatically(account, getContentAuthority(), true);
        accountManager.addAccountExplicitly(account, null, null);

        return account;
    }

    private String getAccountType() {
        return context.getString(R.string.sync_account_type);
    }

    private String getContentAuthority() {
        return context.getString(R.string.content_authority);
    }

    public long insert(T entity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long entityId = insertOperation(db, entity);
        setDataInValid();
        return entityId;
    }

    protected abstract long insertOperation(SQLiteDatabase db, T entity);

    public int bulkInsert(Collection<T> entities) {
        if (entities.size() == 0) {
            return 0;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (T entry : entities) {
                insertOperation(db, entry);
            }
            db.setTransactionSuccessful();
            setDataInValid();
        } finally {
            db.endTransaction();
        }
        return entities.size();
    }

    public boolean delete(T entity) {
        List<T> entities = new ArrayList<>();
        entities.add(entity);
        int numDeleted = bulkDelete(entities);
        return numDeleted == 1;
    }

    public int bulkDelete(Collection<T> entities) {
        Timber.d("Bulk deleting " + entities.size() + " entries");
        if (entities.size() == 0) {
            return 0;
        }
        int numDeleted = deleteOperation(dbHelper.getWritableDatabase(), entities);
        setDataInValid();
        return numDeleted;
    }

    protected abstract int deleteOperation(SQLiteDatabase sb, Collection<T> entities);

    protected String createEntityIdsArgument(Collection<T> entities) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (T entity : entities) {
            checkEntityDeleted(entity);
            sb.append(entity.getId());
            if (i != entities.size() - 1) {
                sb.append(", ");
            }
            i++;
        }
        return sb.toString();
    }

    public void checkEntityDeleted(T entity) {
        if (entity.getSyncState() != SyncState.DELETE_SYNCED) {
            throw new IllegalStateException("An entity needs to be deleted on the backend (DELETE_SYNCED) before it can" +
                    " be removed from the local database");
        }
    }

    public boolean update(T entity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int affected = updateOperation(db, entity);
        setDataInValid();
        return affected != 0;
    }

    protected abstract int updateOperation(SQLiteDatabase db, T entity);

    public int bulkUpdate(Collection<T> entities) {
        Timber.d("Bulk updating " + entities.size() + " entries");
        if (entities.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (T entity : entities) {
                updateOperation(db, entity);
            }
            db.setTransactionSuccessful();
            setDataInValid();
        } finally {
            db.endTransaction();
        }
        return entities.size();
    }

    public long count() {
        return count(null, null);
    }

    public long count(String selection, String[] selectionArgs) {
        return DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), getTableName(), selection, selectionArgs);
    }

    protected abstract String getTableName();

    protected abstract String[] getQueryProjection();

    public long searchForEntityIdBy(String columnName, String searchValue) {
        List<T> entities = query(
                columnName + " = ?",
                new String[]{searchValue},
                null
        );
        if (entities.size() > 1) {
            throw new IllegalArgumentException("The arguments provided returned more than one matching result");
        }
        return entities.size() == 1 ? entities.get(0).getId() : -1;
    }

    public List<T> query() {
        return query(null, null, null);
    }

    public List<T> query(String selection, String[] selectionArgs, String orderBy) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                getTableName(),
                getQueryProjection(),
                selection,
                selectionArgs,
                null, null, orderBy
        );

        List<T> entities = new ArrayList<>();
        while (cursor.moveToNext()) {
            entities.add(fromCursor(cursor));
        }
        cursor.close();
        return entities;
    }

    protected abstract T fromCursor(Cursor cursor);
}
