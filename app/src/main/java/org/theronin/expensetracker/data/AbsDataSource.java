package org.theronin.expensetracker.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.database.DatabaseUtils;
import android.os.Bundle;

import org.apache.commons.lang.NotImplementedException;
import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.R;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public abstract class AbsDataSource<T> {

    public static final String ACCOUNT = "dummyaccount";

    protected CustomApplication application;
    protected DbHelper dbHelper;

    private Set<Observer> observers;

    public AbsDataSource(CustomApplication application) {
        this.application = application;
        dbHelper = DbHelper.getInstance(application);
        this.observers = new HashSet<>();
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

    private String getContentAuthority() {
        return application.getString(R.string.content_authority);
    }

    public long insert(T entity) {
        throw new NotImplementedException();
    }

    public int bulkInsert(Collection<T> entities) {
        throw new NotImplementedException();
    }

    public boolean delete(T entity) {
        throw new NotImplementedException();
    }

    public int bulkDelete(Collection<T> entities) {
        throw new NotImplementedException();
    }

    public boolean update(T entity) {
        throw new NotImplementedException();
    }

    public int bulkUpdate(Collection<T> entities) {
        throw new NotImplementedException();
    }

    public long count() {
        return count(null, null);
    }

    public long count(String selection, String[] selectionArgs) {
        return DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), getTableName(), selection, selectionArgs);
    }

    protected abstract String getTableName();

    public List<T> query() {
        return query(null, null, null);
    }

    public List<T> query(String selection,
                                  String[] selectionArgs,
                                  String orderBy) {
        throw new NotImplementedException();
    }

    public interface Observer {
        void onDataSourceChanged();
    }

    public Account createSyncAccount() {
        Account account = new Account(ACCOUNT, getAccountType());
        AccountManager accountManager = AccountManager.get(application);

        ContentResolver.setSyncAutomatically(account, getContentAuthority(), true);
        accountManager.addAccountExplicitly(account, null, null);

        return account;
    }

    private String getAccountType() {
        return application.getString(R.string.sync_account_type);
    }
}
