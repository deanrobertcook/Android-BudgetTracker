package org.theronin.expensetracker.data.source;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.expensetracker.model.Entity;
import org.theronin.expensetracker.utils.DebugUtils;
import org.theronin.expensetracker.utils.SyncUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public abstract class AbsDataSource<T extends Entity> {

    protected Context context;
    private DbHelper dbHelper;

    /**
     * Useful for when another datasource needs to know about changes in this datasource.
     */
    protected UpdateListener<T> listener;

    private Set<Observer> observers;

    protected void setListener(UpdateListener<T> listener) {
        this.listener = listener;
    }

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
        if (!observers.remove(observer)) {
            throw new IllegalStateException("The observer is not registered to this: " + this.getClass().getSimpleName());
        }
    }

    /**
     * This method should be called after an insert, delete, or update method is called on the data
     * source, to signal any observers that the underlying data source will now be out of date
     */
    public void setDataInValid() {
        Timber.i(this.getClass().toString() + " data set as invalid");
        for (Observer observer : observers) {
            observer.onDataSourceChanged();
        }
        SyncUtils.requestSync(context);
    }

    /**
     * Insert a single Entity into the local database. If the insertion is successful, then the newly
     * created ID is assigned to the entity and that same entity is returned, and also any observers
     * on this DataSource are notified. If the transaction is not successful, then nothing happens
     * (including not notifying any observers)
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
    //TODO consider throwing an Exception for data source operations
    public List<T> bulkInsert(List<T> entities) {
        if (entities.size() == 0) {
            return new ArrayList<>();
        }
        Timber.i("bulkInsert " + entities.size() + " " + entities.get(0).getClass().getSimpleName() + "s");
        DebugUtils.printList(getClass().getName(), entities);
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

    public int bulkDelete(List<T> entities) {
        if (entities.size() == 0) {
            return 0;
        }
        Timber.i("bulkDelete " + entities.size() + " " + entities.get(0).getClass().getSimpleName() + "s");
        DebugUtils.printList(getClass().getName(), entities);
        int numDeleted = deleteOperation(dbHelper.getWritableDatabase(), entities);
        setDataInValid();
        return numDeleted;
    }

    protected abstract int deleteOperation(SQLiteDatabase sb, Collection<T> entities);

    public boolean update(T entity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int affected = updateOperation(db, entity);
        if (affected > 0) {
            setDataInValid();
        }
        return affected > 0;
    }

    protected abstract int updateOperation(SQLiteDatabase db, T entity);

    public int bulkUpdate(List<T> entities) {
        if (entities.size() == 0) {
            return 0;
        }
        Timber.i("bulkUpdate " + entities.size() + " " + entities.get(0).getClass().getSimpleName() + "s");
        DebugUtils.printList(getClass().getName(), entities);
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

    /**
     * An interface for other data sources only
     * TODO this feels a bit hacky. It might be better to think of a BUS system instead
     */
    protected interface UpdateListener<T> {
        void onEntityUpdated(T category);
    }
}
