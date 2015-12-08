package org.theronin.budgettracker.data.loader;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.AbsDataSource;
import org.theronin.budgettracker.data.DataSourceCategory;
import org.theronin.budgettracker.data.DataSourceEntry;
import org.theronin.budgettracker.data.DataSourceExchangeRate;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.task.ExchangeRateDownloadAgent;
import org.theronin.budgettracker.utils.DateUtils;
import org.theronin.budgettracker.utils.MoneyUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class CategoryLoader extends AsyncTaskLoader<List<Category>>
        implements AbsDataSource.Observer, SharedPreferences.OnSharedPreferenceChangeListener {

    private DataSourceCategory dataSourceCategory;
    private DataSourceEntry dataSourceEntry;
    private DataSourceExchangeRate dataSourceExchangeRate;

    private SharedPreferences defaultPreferences;
    private Currency homeCurrency;
    private Map<String, Double> homeCurrencyRates;

    private Context context;

    private String selection;
    private String[] selectionArgs;
    private String orderBy;

    private List<Category> categories;

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

        context = application.getApplicationContext();
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        defaultPreferences.registerOnSharedPreferenceChangeListener(this);
        homeCurrency = MoneyUtils.getHomeCurrency(application, defaultPreferences);

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

            homeCurrencyRates = findHomeCurrencyRates(homeCurrency, allExchangeRates);

            for (Category category : categories) {
                long categoryTotal = 0;
                int missingEntries = 0;

                Iterator<Entry> entryIterator = allEntries.iterator();
                while (entryIterator.hasNext()) {
                    Entry entry = entryIterator.next();
                    Timber.d(entry.toString());
                    if (category.name.equals(entry.category.name)) {
                        entryIterator.remove();
                        if (entry.currency.code.equals(homeCurrency.code)) {
                            Timber.d("Entry currency code matches home currency code");
                            categoryTotal += entry.amount;
                        } else {
                            Timber.d("Entry currency differs from home currency - calculating equivalent value");
                            double entryRate = findExchangeRateForEntry(entry, allExchangeRates);
                            if (entryRate == -1) {
                                Timber.d("Could not find an exchange rate, missingEntries: " + missingEntries);
                                missingEntries++;
                                categoryTotal = -1;
                            } else {
                                Double homeRateForDate = homeCurrencyRates.get(DateUtils.getStorageFormattedDate(entry.utcDate));
                                if (homeRateForDate == null) {
                                    Timber.d("No exchange rate set for the home currency");
                                } else {
                                    Timber.d("Finally: calculating the equivalent entry amount");
                                    double directExchangeRate = homeRateForDate / entryRate;
                                    long convertedEntryAmount = (long) (directExchangeRate * (double) entry.amount);
                                    categoryTotal += convertedEntryAmount;
                                }
                            }
                        }
                    }
                }
                category.setTotal(categoryTotal);
                category.setMissingEntries(missingEntries);
            }
            return categories;
        }
    }

    private Map<String, Double> findHomeCurrencyRates(Currency homeCurrency, List<ExchangeRate> allRates) {
        Map<String, Double> returnRates = new HashMap<>();
        for (ExchangeRate rate: allRates) {
            if (homeCurrency.code.equals(rate.currencyCode)) {
                returnRates.put(DateUtils.getStorageFormattedDate(rate.utcDate), rate.usdRate);
            }
        }
        return returnRates;
    }

    private double findExchangeRateForEntry(Entry entry, List<ExchangeRate> allExchangeRates) {
        ExchangeRate exchangeRate = searchExchangeRates(entry, allExchangeRates);
        if (exchangeRate == null) {
            //rate not found, attempt to download for given day
            List<ExchangeRate> downloadedRates = new ExchangeRateDownloadAgent().downloadExchangeRates(entry.utcDate);
            if (!downloadedRates.isEmpty()) {
                //this will (should) cancel loading ??
                Timber.d("Inserting downloaded exchange rates into the database");
                dataSourceExchangeRate.bulkInsert(downloadedRates);
                exchangeRate = searchExchangeRates(entry, downloadedRates);
            } else {
                Timber.d("There was no exchange rate data for " + DateUtils.getStorageFormattedDate(entry.utcDate));
            }
        } else {
            Timber.d("Exchange rate " + exchangeRate + " found in database");
        }
        return exchangeRate == null ? -1 : exchangeRate.usdRate;
    }

    private ExchangeRate searchExchangeRates(Entry entry, List<ExchangeRate> exchangeRates) {
        for (ExchangeRate rate : exchangeRates) {
            if (DateUtils.sameDay(entry.utcDate, rate.utcDate) &&
                    entry.currency.code.equals(rate.currencyCode)) {
                return rate;
            }
        }
        return null;
    }

    @Override
    public void deliverResult(List<Category> data) {
        this.categories = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (categories != null) {
            deliverResult(categories);
        }

        dataSourceCategory.registerObserver(this);
        dataSourceEntry.registerObserver(this);
        dataSourceExchangeRate.registerObserver(this);

        if (takeContentChanged() || categories == null || categories.isEmpty()) {
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
        homeCurrency = MoneyUtils.getHomeCurrency(context, defaultPreferences);
        if (calculateTotals) {
            forceLoad();
        }
    }
}
