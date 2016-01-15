package org.theronin.expensetracker.data.backend;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.theronin.expensetracker.dagger.InjectedService;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.CurrencySettings;

import javax.inject.Inject;

import timber.log.Timber;

public class ExchangeRateDownloadService extends InjectedService implements CurrencySettings.Listener {

    @Inject AbsDataSource<ExchangeRate> exchangeRateAbsDataSource;
    @Inject AbsDataSource<Entry> entryAbsDataSource;
    @Inject ExchangeRateDownloader downloader;

    private ExchangeRateSyncCoordinator syncCoordinator;

    private CurrencySettings currencySettings;
    private Currency homeCurrency;


    public ExchangeRateDownloadService() {
        super(ExchangeRateDownloadService.class.getName());
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate()");

        currencySettings = new CurrencySettings(this, this);
        homeCurrency = currencySettings.getHomeCurrency();

        syncCoordinator = new ExchangeRateSyncCoordinator(
                entryAbsDataSource, exchangeRateAbsDataSource, downloader, homeCurrency);

        super.onCreate();
    }

    @Override
    public void onHomeCurrencyChanged(Currency homeCurrency) {
        this.homeCurrency = homeCurrency;
    }

    @Override
    public void onCurrentCurrencyChanged(Currency currentCurrency) {

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("onHandleIntent()");

        if (!hasNetworkAccess()) {
            //Just drop the request, the next time we try to calculate rates we'll request it again
            return;
        }

        syncCoordinator.downloadExchangeRates();
    }

    private boolean hasNetworkAccess() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
