package org.theronin.budgettracker.data.loader;

import android.app.Activity;
import android.content.Intent;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.AbsDataSource;
import org.theronin.budgettracker.data.DataSourceCategory;
import org.theronin.budgettracker.data.DataSourceEntry;
import org.theronin.budgettracker.data.DataSourceExchangeRate;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.utils.CurrencySettings;

import java.util.Iterator;
import java.util.List;

import static org.theronin.budgettracker.data.loader.ExchangeRateDownloadService.UTC_DATE_KEY;

public class CategoryLoader extends DataLoader<Category>
        implements AbsDataSource.Observer, CurrencySettings.Listener {

    private final DataSourceCategory dataSourceCategory;
    private final DataSourceEntry dataSourceEntry;
    private final DataSourceExchangeRate dataSourceExchangeRate;

    private final CurrencySettings currencySettings;

    private boolean calculateTotals;

    public CategoryLoader(Activity activity, boolean calculateTotals) {
        super(activity);
        BudgetTrackerApplication application = (BudgetTrackerApplication) activity.getApplication();
        dataSourceCategory = application.getDataSourceCategory();
        dataSourceEntry = application.getDataSourceEntry();
        dataSourceExchangeRate = application.getDataSourceExchangeRate();
        setObservedDataSources(dataSourceCategory, dataSourceEntry, dataSourceExchangeRate);

        currencySettings = new CurrencySettings(activity, this);

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

            CurrencyConverter converter = new CurrencyConverter(currencySettings
                    .getHomeCurrency(), allExchangeRates);
            converter.assignExchangeRatesToEntries(allEntries);

            for (Long date : converter.getMissingExchangeRateDays()) {
                Intent serviceIntent = new Intent(getContext(), ExchangeRateDownloadService.class);
                serviceIntent.putExtra(UTC_DATE_KEY, date);
                getContext().startService(serviceIntent);
            }

            calculateTotals(categories, allEntries);
            return categories;
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
