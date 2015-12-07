package org.theronin.budgettracker.data;

import org.theronin.budgettracker.BudgetTrackerApplication;

import java.util.List;

public abstract class DataSource<T> {
    protected BudgetTrackerApplication application;
    protected BudgetDbHelper dbHelper;

    private boolean dataValid;

    public DataSource(BudgetTrackerApplication application) {
        this.application = application;
        dbHelper = BudgetDbHelper.getInstance(application);
    }

    /**
     * This method should be called after an insert, delete, or update method is called on the data
     * source, to signal that any queries made before this point will be out of date
     */
    public void setDataInValid() {
        dataValid = false;
    }

    /**
     * This method should be called by any subclasses after a query is made, since from that point
     * onwards, provided the data is not invalidated, the query represents the latest data from
     * the data source.
     */
    public void setDataValid() {
        dataValid = true;
    }

    /**
     * @return whether the last query made to the database represents valid data.
     */
    public boolean isValid() {
        return dataValid;
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

    public List<T> query() {
        return query("", null, "");
    }

    public List<T> query(String selection,
                                  String[] selectionArgs,
                                  String orderBy) {
        return null;
    }
}
