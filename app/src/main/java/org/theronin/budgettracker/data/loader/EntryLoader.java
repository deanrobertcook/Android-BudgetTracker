package org.theronin.budgettracker.data.loader;

import android.app.Activity;
import android.content.Intent;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.BudgetContract.EntryView;
import org.theronin.budgettracker.data.DataSourceEntry;
import org.theronin.budgettracker.data.DataSourceExchangeRate;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.utils.CurrencySettings;

import java.util.List;

import timber.log.Timber;

import static org.theronin.budgettracker.data.loader.ExchangeRateDownloadService.UTC_DATE_KEY;

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
        setObservedDataSources(dataSourceEntry, dataSourceExchangeRate);

        currencySettings = new CurrencySettings(activity, this);
    }

    @Override
    public List<Entry> loadInBackground() {
        Timber.d("loadInBackground");
        List<Entry> entries = dataSourceEntry.query(null, null,
                EntryView.COL_DATE + " DESC, " + EntryView._ID + " DESC");
        List<ExchangeRate> allExchangeRates = dataSourceExchangeRate.query();

        final CurrencyConverter converter = new CurrencyConverter(currencySettings
                .getHomeCurrency(), allExchangeRates);
        converter.assignExchangeRatesToEntries(entries);

        for (Long date : converter.getMissingExchangeRateDays()) {
            Intent serviceIntent = new Intent(getContext(), ExchangeRateDownloadService.class);
            serviceIntent.putExtra(UTC_DATE_KEY, date);
            getContext().startService(serviceIntent);
        }

        Timber.d("Returning entries");
        return entries;
    }

    @Override
    public void onHomeCurrencyChanged(Currency homeCurrency) {
        forceLoad();
    }

    @Override
    public void onCurrentCurrencyChanged(Currency currentCurrency) {

    }
}
