package org.theronin.budgettracker.data;

import org.theronin.budgettracker.BudgetTrackerApplication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbsDataSource<T> {
    protected BudgetTrackerApplication application;
    protected BudgetDbHelper dbHelper;

    private Set<Observer> observers;

    public AbsDataSource(BudgetTrackerApplication application) {
        this.application = application;
        dbHelper = BudgetDbHelper.getInstance(application);
        this.observers = new HashSet<>();
    }

    public void registerObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("DataSource.Observers cannot be null");
        }
        this.observers.add(observer);
    }

    public void unregisterObserver(Observer observer) {
        if (!observers.remove(observer)) {
            throw new IllegalStateException("The observer is not registered to this DataSource");
        }
    }

    /**
     * This method should be called after an insert, delete, or update method is called on the data
     * source, to signal any observers that the underlying data source will now be out of date
     */
    public void setDataInValid() {
        for (Observer observer : observers) {
            observer.onDataSourceChanged();
        }
    }

    public long insert(T entity) {
        return -1;
    }

    public int bulkInsert(List<T> entities) {
        return -1;
    }

    public boolean delete(T entity) {
        return false;
    }

    public boolean update(T entity) {
        return false;
    }

    public List<T> query(String selection,
                                  String[] selectionArgs,
                                  String orderBy) {
        return null;
    }

    public interface Observer {
        void onDataSourceChanged();
    }
}
