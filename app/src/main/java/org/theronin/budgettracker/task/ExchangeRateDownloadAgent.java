package org.theronin.budgettracker.task;

import android.os.AsyncTask;

import org.theronin.budgettracker.model.ExchangeRate;

import java.util.List;

public class ExchangeRateDownloadAgent {
    private static final String TAG = ExchangeRateDownloadAgent.class.getName();

    private static final String OPEN_EXCHANGE_BASE_URL = "https://openexchangerates.org/api/";

    //TODO don't commit this to github!
    private static final String API_KEY = "";


    private class ExchangeRateDownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public interface Listener {
        void onExchangeRatesDownloaded(List<ExchangeRate> rates);
    }
}
