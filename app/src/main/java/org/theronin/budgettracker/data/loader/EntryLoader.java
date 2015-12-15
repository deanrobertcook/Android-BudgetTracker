package org.theronin.budgettracker.data.loader;

import android.app.Activity;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.R;
import org.theronin.budgettracker.data.BudgetContract.EntryView;
import org.theronin.budgettracker.data.DataSourceEntry;
import org.theronin.budgettracker.data.DataSourceExchangeRate;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.utils.CurrencySettings;

import java.util.List;

import timber.log.Timber;

public class EntryLoader extends DataLoader<Entry> implements CurrencySettings.Listener {

    private final DataSourceEntry dataSourceEntry;
    private final DataSourceExchangeRate dataSourceExchangeRate;

    private final CurrencySettings currencySettings;
    private final ExchangeRateDownloader downloader;

    public EntryLoader(Activity activity) {
        super(activity);
        dataSourceEntry = ((BudgetTrackerApplication) activity.getApplication())
                .getDataSourceEntry();
        dataSourceExchangeRate = ((BudgetTrackerApplication) activity.getApplication())
                .getDataSourceExchangeRate();
        setObservedDataSources(dataSourceEntry, dataSourceExchangeRate);

        currencySettings = new CurrencySettings(activity, this);
        String[] supportedCurrencies = getContext().getResources()
                .getStringArray(R.array.currency_codes);
        downloader = new ExchangeRateDownloader(supportedCurrencies, dataSourceExchangeRate);

    }

    @Override
    public List<Entry> loadInBackground() {
        List<Entry> entries = dataSourceEntry.query(null, null,
                EntryView.COL_DATE + " DESC, " + EntryView._ID + " DESC");
        List<ExchangeRate> allExchangeRates = dataSourceExchangeRate.query();

        final CurrencyConverter converter =
                new CurrencyConverter(currencySettings.getHomeCurrency(), allExchangeRates);
        converter.assignExchangeRatesToEntries(entries);

        if (!converter.getMissingExchangeRateDays().isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //TODO this should be a form of singleton that only accepts one request at a time
                    downloader.downloadExchangeRateDataForDays(converter.getMissingExchangeRateDays());
                }
            }).start();
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
