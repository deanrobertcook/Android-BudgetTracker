package org.theronin.expensetracker.data.loader;

import android.content.Context;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.backend.SyncState;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.CurrencySettings;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class EntryLoader extends DataLoader<Entry> implements CurrencySettings.Listener {

    @Inject AbsDataSource<Entry> entryDataSource;
    @Inject AbsDataSource<ExchangeRate> exchangeRateDataSource;

    private final CurrencySettings currencySettings;

    public EntryLoader(Context context, InjectedComponent component) {
        super(context, component);
        setObservedDataSources(entryDataSource, exchangeRateDataSource);

        currencySettings = new CurrencySettings(context, this);
    }

    @Override
    public List<Entry> loadInBackground() {
        Timber.d("loadInBackground");
        List<Entry> entries = entryDataSource.query(
                EntryView.COL_SYNC_STATUS + " NOT IN (" + SyncState.deleteStateSelection() + ")", null,
                EntryView.COL_DATE + " DESC, " + EntryView._ID + " DESC");
        List<ExchangeRate> allExchangeRates = exchangeRateDataSource.query();

        final CurrencyConverter converter = new CurrencyConverter(currencySettings.getHomeCurrency(), allExchangeRates);
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
