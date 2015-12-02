package org.theronin.budgettracker.task;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.theronin.budgettracker.model.ExchangeRate;
import org.theronin.budgettracker.utils.DateUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExchangeRateDownloadAgent {
    private static final String TAG = ExchangeRateDownloadAgent.class.getName();

    private static final String OPEN_EXCHANGE_BASE_URL = "https://openexchangerates.org/api/";

    private static final String RATES_KEY = "rates";

    //TODO don't commit this to github!
    private static final String API_KEY = "";

    private long utcDate;
    private Listener listener;

    public void getExchangeData(long utcDate, Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener cannot be null");
        }
        this.utcDate = utcDate;
        this.listener = listener;
        new ExchangeRateDownloadTask().execute(utcDate);
    }


    private class ExchangeRateDownloadTask extends AsyncTask<Long, Void, String> {

        @Override
        protected String doInBackground(Long... params) {
            String formattedDate = DateUtils.getStorageFormattedDate(params[0]);

            String urlString = OPEN_EXCHANGE_BASE_URL + "historical/" + formattedDate + ".json?app_id=" + API_KEY;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String exchangeDataString = null;

            try {
                URL url = new URL(urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    exchangeDataString = "";
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    exchangeDataString = "";
                }
                exchangeDataString = buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return exchangeDataString;
        }

        @Override
        protected void onPostExecute(String exchangeRatesJson) {
            Gson gson = new Gson();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            
            Set<Map.Entry<String, JsonElement>> entries =
                    ((JsonObject) gson.fromJson(exchangeRatesJson, JsonElement.class))
                    .getAsJsonObject(RATES_KEY).entrySet();

            for (Map.Entry<String, JsonElement> entry: entries) {
                exchangeRates.add(new ExchangeRate(
                        entry.getKey(),
                        utcDate,
                        Double.parseDouble(entry.getValue().toString()))
                );
            }
            listener.onExchangeRatesDownloaded(exchangeRates);
        }
    }

    public interface Listener {
        void onExchangeRatesDownloaded(List<ExchangeRate> rates);
    }
}
