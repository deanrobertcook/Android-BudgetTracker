package org.theronin.expensetracker.data.loader;

import android.app.Activity;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.sync.SyncState;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.CurrencySettings;

import java.util.List;

import timber.log.Timber;

public class EntryLoader extends DataLoader<Entry> implements CurrencySettings.Listener {

    private CustomApplication app;

    private final CurrencySettings currencySettings;

    public EntryLoader(Activity activity) {
        super(activity);
        app = ((CustomApplication) activity.getApplication());

        setObservedDataSources(
                app.getDataSourceEntry(),
                app.getDataSourceExchangeRate());

        currencySettings = new CurrencySettings(activity, this);
    }

    @Override
    public List<Entry> loadInBackground() {
        Timber.d("loadInBackground");
        List<Entry> entries = app.getDataSourceEntry().query(
                EntryView.COL_SYNC_STATUS + " NOT IN (" + SyncState.deleteStateSelection() + ")", null,
                EntryView.COL_DATE + " DESC, " + EntryView._ID + " DESC");
        List<ExchangeRate> allExchangeRates = app.getDataSourceExchangeRate().query();

        final CurrencyConverter converter = new CurrencyConverter(currencySettings
                .getHomeCurrency(), allExchangeRates);
        converter.assignExchangeRatesToEntries(entries);

//        for (Long date : converter.getMissingExchangeRateDays()) {
//            Intent serviceIntent = new Intent(getContext(), ExchangeRateDownloadService.class);
//            serviceIntent.putExtra(UTC_DATE_KEY, date);
//            getContext().startService(serviceIntent);
//        }

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
