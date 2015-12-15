package org.theronin.budgettracker.data.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.theronin.budgettracker.data.AbsDataSource;

import java.util.List;

import timber.log.Timber;

public abstract class DataLoader<T> extends AsyncTaskLoader<List<T>>
    implements AbsDataSource.Observer {

    protected AbsDataSource[] dataSources;
    private List<T> data;

    public DataLoader(Context context) {
        super(context);
    }

    protected void setObservedDataSources(AbsDataSource... dataSources) {
        this.dataSources = dataSources;
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

        registerToDataSources();

        if (takeContentChanged() || data == null || data.isEmpty()) {
            forceLoad();
        }
    }

    private void registerToDataSources() {
        if (dataSources.length == 0) {
            throw new IllegalStateException("There needs to be at least one Datasource that this" +
                    " loader is registered to");
        }
        for (AbsDataSource absDataSource : dataSources) {
            absDataSource.registerObserver(this);
        }
    }

    @Override
    public void onDataSourceChanged() {
        Timber.d(this.getClass().toString() + " onDataSourceChanged()");
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        unregisterFromDataSources();
    }

    private void unregisterFromDataSources() {
        for (AbsDataSource absDataSource : dataSources) {
            absDataSource.unregisterObserver(this);
        }
    }

}
