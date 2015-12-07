package org.theronin.budgettracker.data;

import android.content.Context;

import java.util.List;

public abstract class DataSource<T> {
    protected BudgetDbHelper dbHelper;

    public DataSource(Context context) {
        dbHelper = BudgetDbHelper.getInstance(context);
    }

    public abstract int insert(T entity);
    public abstract int delete(T entity);
    public abstract int update(T entity);
    public abstract List<T> query();
    public abstract List<T> query(String selection,
                                  String[] selectionArgs,
                                  String orderBy);
}
