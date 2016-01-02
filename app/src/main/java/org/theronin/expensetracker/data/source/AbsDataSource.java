package org.theronin.expensetracker.data.source;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
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
    private DbHelper dbHelper;

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

    /**
     * Insert a single Entity into the local database. If the insertion is successful, then the newly
     * created ID is assigned to the entity and that same entity is returned, and also any observers
     * on this DataSource are notified. If the transaction is not successful, then nothing happens
     * (including not observing notification)
     * @param entity the newly created entity to insert into the database.
     * @return the same entity with it's new local DB ID if it was inserted successfully.
     */
    public T insert(T entity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            entity.setId(insertOperation(db, entity));
            setDataInValid();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entity;
    }

    protected abstract long insertOperation(SQLiteDatabase db, T entity);

    /**
     * Insert a collection of entities into the local database. For every entity that is successfully
     * inserted, the newly created id is assigned to that entity and any observers to this DataSource
     * are notified. If one insertion fails, the entire transaction is rolled back and nothing happens
     * (including observer notification).
     * @param entities The collection of entities to insert into the database
     * @return the same collection of entities, but with local IDs set if all entities were inserted
     * successfully
     */
    public List<T> bulkInsert(List<T> entities) {
        if (entities.size() == 0) {
            return new ArrayList<>();
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (T entity : entities) {
                entity.setId(insertOperation(db, entity));
            }
            db.setTransactionSuccessful();
            setDataInValid();
        } finally {
            db.endTransaction();
        }

        return entities;
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

    protected String createEntityIdsInClause(Collection<T> entities) {
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

    protected String createEntityGlobalIdsInClause(Collection<T> entities) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (T entity : entities) {
            checkEntityDeleted(entity);
            sb.append(String.format("'%s'", entity.getGlobalId()));
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

    protected abstract ContentValues getContentValues(T entity);

    protected abstract String getTableName();

    protected abstract String[] getQueryProjection();

    public long getId(T entity) {
        List<T> entities = searchForIdFromEntity(entity);
        if (entities.size() > 1) {
            throw new IllegalArgumentException("The arguments provided returned more than one matching result");
        }
        return entities.size() == 1 ? entities.get(0).getId() : -1;
    }

    protected abstract List<T> searchForIdFromEntity(T entity);

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
