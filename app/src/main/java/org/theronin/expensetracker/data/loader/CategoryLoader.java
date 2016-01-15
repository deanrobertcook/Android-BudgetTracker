package org.theronin.expensetracker.data.loader;

import android.content.Context;
import android.content.Intent;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.backend.ExchangeRateDownloadService;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.CurrencySettings;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

public class CategoryLoader extends DataLoader<Category>
        implements AbsDataSource.Observer, CurrencySettings.Listener {

    private final CurrencySettings currencySettings;

    private boolean calculateTotals;

    @Inject AbsDataSource<Category> categoryDataSource;
    @Inject AbsDataSource<ExchangeRate> exchangeRateDataSource;
    @Inject AbsDataSource<Entry> entryDataSource;

    public CategoryLoader(Context context, InjectedComponent component, boolean calculateTotals) {
        super(context, component);
        setObservedDataSources(categoryDataSource, exchangeRateDataSource, entryDataSource);
        currencySettings = new CurrencySettings(context, this);
        this.calculateTotals = calculateTotals;
    }

    @Override
    public List<Category> loadInBackground() {
        List<Category> categories = categoryDataSource.query();
        if (!calculateTotals) {
            return categories;
        } else {
            List<Entry> allEntries = entryDataSource.query();
            List<ExchangeRate> allExchangeRates = exchangeRateDataSource.query();

            CurrencyConverter converter = new CurrencyConverter(currencySettings
                    .getHomeCurrency(), allExchangeRates);
            converter.assignExchangeRatesToEntries(allEntries);

            if (!converter.getMissingExchangeRateDays().isEmpty()) {
                Intent serviceIntent = new Intent(getContext(), ExchangeRateDownloadService.class);
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
                        categoryTotal += Math.round((double) entry.amount * entry.getDirectExchangeRate());
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
