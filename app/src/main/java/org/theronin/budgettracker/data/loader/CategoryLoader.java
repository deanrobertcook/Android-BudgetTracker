package org.theronin.budgettracker.data.loader;

import android.app.Activity;

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
import org.theronin.budgettracker.utils.CurrencySettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CategoryLoader extends DataLoader<Category>
        implements AbsDataSource.Observer, CurrencySettings.Listener {

    private DataSourceCategory dataSourceCategory;
    private DataSourceEntry dataSourceEntry;
    private DataSourceExchangeRate dataSourceExchangeRate;

    private CurrencySettings currencySettings;

    private String[] supportedCurrencies;

    private boolean calculateTotals;

    public CategoryLoader(Activity activity, boolean calculateTotals) {
        super(activity);
        BudgetTrackerApplication application = (BudgetTrackerApplication) activity.getApplication();
        dataSourceCategory = application.getDataSourceCategory();
        dataSourceEntry = application.getDataSourceEntry();
        dataSourceExchangeRate = application.getDataSourceExchangeRate();
        setDataSources(dataSourceCategory, dataSourceEntry, dataSourceExchangeRate);

        currencySettings = new CurrencySettings(activity, this);

        supportedCurrencies = getContext().getResources().getStringArray(R.array.currency_codes);

        this.calculateTotals = calculateTotals;
    }

    @Override
    public List<Category> loadInBackground() {
        List<Category> categories = dataSourceCategory.query();
        if (!calculateTotals) {
            return categories;
        } else {
            List<Entry> allEntries = dataSourceEntry.query();
            List<ExchangeRate> allExchangeRates = dataSourceExchangeRate.query();

            CurrencyConverter converter =
                    new CurrencyConverter(currencySettings.getHomeCurrency(), allExchangeRates);
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
    public void onHomeCurrencyChanged(Currency homeCurrency) {
        if (calculateTotals) {
            forceLoad();
        }
    }

    @Override
    public void onCurrentCurrencyChanged(Currency currentCurrency) {
    }
}
