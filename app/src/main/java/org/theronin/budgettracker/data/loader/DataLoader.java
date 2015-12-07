package org.theronin.budgettracker.data.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.theronin.budgettracker.data.AbsDataSource;

import java.util.List;

public class DataLoader<T> extends AsyncTaskLoader<List<T>>
    implements AbsDataSource.Observer {

    protected AbsDataSource<T> dataSource;
    private List<T> data;

    private String selection;
    private String[] selectionArgs;
    private String orderBy;

    @Override
    public List<T> loadInBackground() {
        return dataSource.query(selection, selectionArgs, orderBy);
    }

    public DataLoader(Context context, AbsDataSource<T> dataSource) {
        this(context, dataSource, null, null, null);
    }

    public DataLoader(Context context,
                      AbsDataSource<T> dataSource,
                      String selection,
                      String[] selectionArgs,
                      String orderBy) {
        super(context);
        this.dataSource = dataSource;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.orderBy = orderBy;
    }

    @Override
    public void deliverResult(List<T> data) {
        this.data = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        }

        dataSource.registerObserver(this);

        if (takeContentChanged() || data == null || data.isEmpty()) {
            forceLoad();
        }
    }

    @Override
    public void onDataSourceChanged() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        dataSource.unregisterObserver(this);
    }
}
