package org.theronin.expensetracker.data.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.source.AbsDataSource;

import java.util.List;

import timber.log.Timber;

public abstract class DataLoader<T> extends AsyncTaskLoader<List<T>>
    implements AbsDataSource.Observer {

    protected AbsDataSource[] dataSources;
    private List<T> data;

    public DataLoader(Context context, InjectedComponent component) {
        super(context);
        component.inject(this);
    }

    protected void setObservedDataSources(AbsDataSource... dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public void deliverResult(List<T> data) {
        Timber.d(this.getClass().getSimpleName() + " deliverResults()");
        this.data = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        Timber.d(this.getClass().getSimpleName() + " onStartLoading()");
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
            Timber.i(this.getClass().getSimpleName() + " registering to " + absDataSource.getClass().getSimpleName());
            absDataSource.registerObserver(this);
        }
    }

    @Override
    public void onDataSourceChanged() {
        Timber.i(this.getClass().getSimpleName() + " onDataSourceChanged()");
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        Timber.i(this.getClass().getSimpleName() + " onStopLoading()");
        cancelLoad();
    }

    @Override
    protected void onReset() {
        Timber.i(this.getClass().getSimpleName() + " onReset()");
        onStopLoading();
        unregisterFromDataSources();
    }

    private void unregisterFromDataSources() {
        for (AbsDataSource absDataSource : dataSources) {
            Timber.i(this.getClass().getSimpleName() + " unregistering from " + absDataSource.getClass().getSimpleName());
            absDataSource.unregisterObserver(this);
        }
    }
}
