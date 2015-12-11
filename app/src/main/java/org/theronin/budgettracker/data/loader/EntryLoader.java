package org.theronin.budgettracker.data.loader;

import android.app.Activity;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.BudgetContract.EntryView;
import org.theronin.budgettracker.data.DataSourceEntry;
import org.theronin.budgettracker.data.DataSourceExchangeRate;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.utils.CurrencySettings;

import java.util.List;

public class EntryLoader extends DataLoader<Entry> implements CurrencySettings.Listener {

    private final DataSourceEntry dataSourceEntry;
    private final DataSourceExchangeRate dataSourceExchangeRate;

    private final CurrencySettings currencySettings;

    public EntryLoader(Activity activity) {
        super(activity);
        dataSourceEntry = ((BudgetTrackerApplication) activity.getApplication())
                .getDataSourceEntry();
        dataSourceExchangeRate = ((BudgetTrackerApplication) activity.getApplication())
                .getDataSourceExchangeRate();
        setDataSources(dataSourceEntry);

        currencySettings = new CurrencySettings(activity, this);
    }

    @Override
    public List<Entry> loadInBackground() {
        List<Entry> entries = dataSourceEntry.query(null, null,
                EntryView.COL_DATE + " DESC, " + EntryView._ID + " DESC");
        List<ExchangeRate> allExchangeRates = dataSourceExchangeRate.query();

        CurrencyConverter currencyConverter =
                new CurrencyConverter(currencySettings.getHomeCurrency(), allExchangeRates);
        currencyConverter.assignExchangeRatesToEntries(entries);
        return entries;
    }

    @Override
    public void onHomeCurrencyChanged(Currency homeCurrency) {
        forceLoad();
    }
}
