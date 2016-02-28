package org.theronin.expensetracker.data.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;

import org.theronin.expensetracker.data.backend.exchangerate.ExchangeRateDownloadService;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceExchangeRate;
import org.theronin.expensetracker.data.source.DbHelper;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.model.user.UserManager;
import org.theronin.expensetracker.utils.Prefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public abstract class DataLoader<T> extends AsyncTaskLoader<List<T>> implements
        AbsDataSource.Observer,
        CurrencyConverter.Callback {

    protected final List<AbsDataSource> dataSources;

    protected final CurrencyConverter currencyConverter;

    private AbsDataSource<ExchangeRate> exchangeRateDataSource;
    private List<T> data;

    public DataLoader(Context context) {
        super(context);
        Timber.tag(getClass().getName()).v("DataLoader initialising.");

        exchangeRateDataSource = new DataSourceExchangeRate(getContext(),
                DbHelper.getInstance(getContext(), UserManager.getUser(getContext()).getId()));
        this.dataSources = new ArrayList<>();
        setObservedDataSources(exchangeRateDataSource);

        currencyConverter = new CurrencyConverter(this, Prefs.getHomeCurrency(context));
    }

    /**
     * This functionality is required by both the Category and Entry loader. It takes all of the
     * exchange rates and all of the entries that the respective loaders need to return to their
     * clients, calculates the amount each entry would be worth in the home currency, and then
     * assigns that value to the entry.
     * @param allEntries the entries for which we should calculate home amounts
     */
    protected void assignHomeAmountsToEntries(List<Entry> allEntries) {
        List<ExchangeRate> allExchangeRates = exchangeRateDataSource.query();
        currencyConverter.assignExchangeRatesToEntries(allExchangeRates, allEntries);
    }

    protected void setObservedDataSources(AbsDataSource... dataSources) {
        this.dataSources.addAll(Arrays.asList(dataSources));
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
        if (dataSources.isEmpty()) {
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

    @Override
    public void needToDownloadExchangeRates() {
        Intent serviceIntent = new Intent(getContext(), ExchangeRateDownloadService.class);
        getContext().startService(serviceIntent);
    }
}
