package org.theronin.budgettracker.data.loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.theronin.budgettracker.data.DataSourceExchangeRate;
import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.utils.DateUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class ExchangeRateDownloader {
    public static final String TAG = ExchangeRateDownloader.class.getName();

    protected static final String OPEN_EXCHANGE_URL = "https://openexchangerates" +
            ".org/api/historical/%s.json?app_id=%s";
    //TODO don't commit this to github!
    protected static final String API_KEY = "";

    private static final String RATES_KEY = "rates";

    private final Set<String> currenciesToSave;

    private final DataSourceExchangeRate dataSource;

    public ExchangeRateDownloader(String[] currenciesToSave, DataSourceExchangeRate dataSource) {
        if (currenciesToSave == null || currenciesToSave.length == 0) {
            throw new IllegalArgumentException("currenciesToSave cannot be null and must contain" +
                    " at least one currency to save");
        }
        this.currenciesToSave = new HashSet<>(Arrays.asList(currenciesToSave));
        this.dataSource = dataSource;
    }

    public void downloadExchangeRateDataForDays(List<Long> days) {
        for (Long utcDate : days) {
            List<ExchangeRate> downloadedRates = downloadExchangeRatesOnDay(utcDate);
            if (!downloadedRates.isEmpty()) {
                dataSource.bulkInsert(downloadedRates);
            }
        }
        Timber.d("All days finished downloading");
    }

    public List<ExchangeRate> downloadExchangeRatesOnDay(long utcDate) {
        //TODO check the network connection
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
        for (String currencyCode : currenciesToSave) {
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
