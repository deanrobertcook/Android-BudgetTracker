package org.theronin.budgettracker.data.loader;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.AbsDataSource;
import org.theronin.budgettracker.data.DataSourceCategory;
import org.theronin.budgettracker.data.DataSourceEntry;
import org.theronin.budgettracker.data.DataSourceExchangeRate;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

public class CategoryLoader extends AsyncTaskLoader<List<Category>>
        implements AbsDataSource.Observer, SharedPreferences.OnSharedPreferenceChangeListener {

    private DataSourceCategory dataSourceCategory;
    private DataSourceEntry dataSourceEntry;
    private DataSourceExchangeRate dataSourceExchangeRate;

    private SharedPreferences defaultPreferences;
    private Currency homeCurrency;
    private String[] supportedCurrencies;

    private String selection;
    private String[] selectionArgs;
    private String orderBy;

    private List<Category> data;

    private boolean calculateTotals;

    public CategoryLoader(Activity activity,
                          String selection,
                          String[] selectionArgs,
                          String orderBy,
                          boolean calculateTotals) {
        super(activity);
        BudgetTrackerApplication application = (BudgetTrackerApplication) activity.getApplication();
        dataSourceCategory = application.getDataSourceCategory();
        dataSourceEntry = application.getDataSourceEntry();
        dataSourceExchangeRate = application.getDataSourceExchangeRate();

        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        defaultPreferences.registerOnSharedPreferenceChangeListener(this);
        homeCurrency = MoneyUtils.getHomeCurrency(application, defaultPreferences);
        supportedCurrencies = getContext().getResources().getStringArray(R.array.currency_codes);

        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.orderBy = orderBy;
        this.calculateTotals = calculateTotals;
    }

    @Override
    public List<Category> loadInBackground() {
        List<Category> categories = dataSourceCategory.query(selection, selectionArgs, orderBy);
        if (!calculateTotals) {
            return categories;
        } else {
            List<Entry> allEntries = dataSourceEntry.query();
            List<ExchangeRate> allExchangeRates = dataSourceExchangeRate.query();

            CurrencyConverter converter = new CurrencyConverter(homeCurrency, allExchangeRates);
            converter.assignExchangeRatesToEntries(allEntries);

            if (converter.getMissingExchangeRateDays().isEmpty()) {
                calculateTotals(categories, allEntries);
                return categories;
            } else {
                downloadMissingData(converter.getMissingExchangeRateDays());
                //the data is not ready, just return a blank list
                //this thread will be started again when the data is inserted
                return new ArrayList<>();
            }
        }
    }

    protected void calculateTotals(List<Category> allCategories, List<Entry> allEntries) {
        for (Category category : allCategories) {
            long categoryTotal = 0;
            int missingEntries = 0;
            Iterator<Entry> entryIterator = allEntries.iterator();
            while (entryIterator.hasNext()) {
                Entry entry = entryIterator.next();
                if (category.name.equals(entry.category.name)) {
                    entryIterator.remove();
                    if (entry.getDirectExchangeRate() == -1.0) {
                        //TODO could have a more elegant way of handling missing entry rate data
                        //But for now I'll just drop them from the calculation
                        missingEntries++;
                    } else {
                        categoryTotal += entry.amount * entry.getDirectExchangeRate();
                    }
                }
            }
            category.setTotal(categoryTotal);
            category.setMissingEntries(missingEntries);
        }
    }

    private void downloadMissingData(List<Long> missingExchangeRateDays) {
        for (Long utcDate : missingExchangeRateDays) {
            List<ExchangeRate> downloadedRates =
                    new ExchangeRateDownloader(supportedCurrencies).downloadExchangeRates(utcDate);
            if (!downloadedRates.isEmpty()) {
                dataSourceExchangeRate.bulkInsert(downloadedRates);
            }
        }
    }

    @Override
    public void deliverResult(List<Category> data) {
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

        dataSourceCategory.registerObserver(this);
        dataSourceEntry.registerObserver(this);
        dataSourceExchangeRate.registerObserver(this);

        if (takeContentChanged() || data == null || data.isEmpty()) {
            forceLoad();
        }
    }

    @Override
    public void onDataSourceChanged() {
        Timber.d("onDataSourceChanged");
        cancelLoad();
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        dataSourceCategory.unregisterObserver(this);
        dataSourceEntry.unregisterObserver(this);
        dataSourceExchangeRate.unregisterObserver(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        homeCurrency = MoneyUtils.getHomeCurrency(getContext(), defaultPreferences);
        if (calculateTotals) {
            forceLoad();
        }
    }
}
