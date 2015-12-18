package org.theronin.expensetracker.data.loader;

import android.app.IntentService;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.DataSourceExchangeRate;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ExchangeRateDownloadService extends IntentService {

    public static String UTC_DATE_KEY = ExchangeRateDownloadService.class.getName() + ":UTC_DATE_KEY";

    private DataSourceExchangeRate dataSourceExchangeRate;

    private List<Long> utcDatesQueuedToDownload;

    protected static final String OPEN_EXCHANGE_URL = "https://openexchangerates" +
            ".org/api/historical/%s.json?app_id=%s";
    //TODO don't commit this to github!
    protected static final String API_KEY = "";

    private static final String RATES_KEY = "rates";

    public ExchangeRateDownloadService() {
        super(ExchangeRateDownloadService.class.getName());
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate()");
        this.utcDatesQueuedToDownload = new ArrayList<>();
        this.dataSourceExchangeRate = ((CustomApplication) getApplication())
                .getDataSourceExchangeRate();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand");

        Long utcDateToDownload = intent.getLongExtra(UTC_DATE_KEY, -1);
        if (DateUtils.listContainsDate(utcDatesQueuedToDownload, utcDateToDownload)) {
            intent.removeExtra(UTC_DATE_KEY); //marks as pointless to download
            Timber.d("Data for date already in queue: neuter the intent");
        } else {
            utcDatesQueuedToDownload.add(utcDateToDownload);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("onHandleIntent()");

        if (!hasNetworkAccess()) {
            //Just drop the request, the next time we try to calculate rates we'll request it again
            return;
        }

        if (intent.hasExtra(UTC_DATE_KEY)) {
            Long utcDate = intent.getLongExtra(UTC_DATE_KEY, -1);
            if(utcDate == -1) {
                throw new IllegalStateException("The date should not be -1 at this point");
            }
            Timber.d("Downloading for date: " + DateUtils.getStorageFormattedDate(utcDate) +
                    " (" + utcDate + ")");

            try {
                List<ExchangeRate> downloadedRates = downloadExchangeRatesOnDay(utcDate);
                if (!downloadedRates.isEmpty()) {
                    Timber.d("Inserting entries into the database");
                    dataSourceExchangeRate.bulkInsert(downloadedRates);
                }
                utcDatesQueuedToDownload.remove(utcDate);
            } catch (Exception e) {
                e.printStackTrace();
                onDownloadFailed();
                return;
            }
        }
    }

    private void onDownloadFailed() {
        //TODO show warning to users that today's exchange rate could not be downloaded
        return;
    }

    private boolean hasNetworkAccess() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private List<ExchangeRate> downloadExchangeRatesOnDay(long utcDate) {
        URL url = buildUrlFromTimestamp(utcDate);
        String jsonString = downloadJson(url);
        return getRatesFromJson(jsonString, utcDate);
    }

    protected String downloadJson(URL url) {
        URLConnection urlConnection = getConnectedUrlConnection(url);
        BufferedReader reader = getBufferedReader(urlConnection);
        if (reader == null) {
            return null;
        }

        try {
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.length() == 0 ? null : sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (urlConnection instanceof HttpURLConnection) {
                ((HttpURLConnection) urlConnection).disconnect();
            }
        }
        return null;
    }

    protected URL buildUrlFromTimestamp(long utcDate) {
        String formattedDate = DateUtils.getStorageFormattedDate(utcDate);
        String urlString = String.format(OPEN_EXCHANGE_URL, formattedDate, API_KEY);
        Timber.d("urlString: " + urlString);
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected URLConnection getConnectedUrlConnection(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("Provided url cannot be null");
        }
        try {
            URLConnection urlConnection = url.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                ((HttpURLConnection) urlConnection).setRequestMethod("GET");
            }
            urlConnection.connect();
            return urlConnection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected BufferedReader getBufferedReader(URLConnection urlConnection) {
        if (urlConnection == null) {
            throw new IllegalArgumentException("urlConnection cannot be null");
        }
        try {
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            return new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected List<ExchangeRate> getRatesFromJson(String exchangeRatesJson, long utcDate) {
        if (exchangeRatesJson == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        JsonObject ratesObject = ((JsonObject) gson.fromJson(exchangeRatesJson, JsonElement.class))
                .getAsJsonObject(RATES_KEY);

        List<ExchangeRate> exchangeRates = new ArrayList<>();

        String[] supportedCurrencies = getResources()
                .getStringArray(R.array.currency_codes);
        for (String currencyCode : supportedCurrencies) {
            double usdRate = -1.0;
            if (ratesObject != null && ratesObject.has(currencyCode)) {
                usdRate = ratesObject.get(currencyCode).getAsDouble();
            }

            exchangeRates.add(new ExchangeRate(
                            currencyCode,
                            utcDate,
                            usdRate,
                            System.currentTimeMillis())
            );
        }
        return exchangeRates;
    }
}
