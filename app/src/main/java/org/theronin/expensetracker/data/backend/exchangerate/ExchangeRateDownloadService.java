package org.theronin.expensetracker.data.backend.exchangerate;

import android.app.IntentService;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DataSourceExchangeRate;
import org.theronin.expensetracker.data.source.DbHelper;
import org.theronin.expensetracker.model.user.UserManager;
import org.theronin.expensetracker.utils.Prefs;

public class ExchangeRateDownloadService extends IntentService {
    private boolean isStarted = false;

    private ExchangeRateSyncCoordinator syncCoordinator;

    public ExchangeRateDownloadService() {
        super(ExchangeRateDownloadService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DbHelper helper = DbHelper.getInstance(this, UserManager.getUser(this).getId());
        syncCoordinator = new ExchangeRateSyncCoordinator(
                DataSourceEntry.newInstance(this, helper),
                new DataSourceExchangeRate(this, helper),
                new ParseExchangeRateDownloader(),
                Prefs.getHomeCurrency(this));

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

        syncCoordinator.downloadExchangeRates();
    }

    private boolean hasNetworkAccess() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
