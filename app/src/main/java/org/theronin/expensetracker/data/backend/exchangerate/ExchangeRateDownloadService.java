package org.theronin.expensetracker.data.backend.exchangerate;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.theronin.expensetracker.dagger.InjectedService;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.Prefs;

import javax.inject.Inject;

import timber.log.Timber;

public class ExchangeRateDownloadService extends InjectedService {

    @Inject AbsDataSource<ExchangeRate> exchangeRateAbsDataSource;
    @Inject AbsDataSource<Entry> entryAbsDataSource;
    @Inject ExchangeRateDownloader downloader;

    private boolean isStarted = false;

    private ExchangeRateSyncCoordinator syncCoordinator;

    private Currency homeCurrency;

    public ExchangeRateDownloadService() {
        super(ExchangeRateDownloadService.class.getName());
    }

    @Override
    public void onCreate() {
        Timber.i("onCreate()");
        super.onCreate();

        homeCurrency = Prefs.getHomeCurrency(this);

        syncCoordinator = new ExchangeRateSyncCoordinator(
                entryAbsDataSource, exchangeRateAbsDataSource, downloader, homeCurrency);

        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!hasNetworkAccess()) {
            //Just drop the request, the next time we try to calculate rates we'll request it again
            return;
        }

        if (isStarted) {
            return; //drop any incoming intents once started
        }
        isStarted = true;

        Timber.i("Performing download of exchange rates");
        syncCoordinator.downloadExchangeRates();
    }

    private boolean hasNetworkAccess() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
