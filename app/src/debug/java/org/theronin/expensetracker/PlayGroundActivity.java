package org.theronin.expensetracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.theronin.expensetracker.data.backend.ParseExchangeRateDownloader;
import org.theronin.expensetracker.model.ExchangeRate;

import java.util.List;

import timber.log.Timber;


public class PlayGroundActivity extends AppCompatActivity implements ParseExchangeRateDownloader.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_playground);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
//        toolbar.setTitle("PlayGround Activity");
//        setSupportActionBar(toolbar);
//
//        ParseExchangeRateDownloader downloader = new ParseExchangeRateDownloader();
//
//        List<Currency> currencies = Arrays.asList(
//                new Currency("AUD"),
//                new Currency("USD"),
//                new Currency("EUR"),
//                new Currency("WOOP")
//        );
//        List<Long> dates = Arrays.asList(
//                System.currentTimeMillis(),
//                System.currentTimeMillis() - (24 * 60L * 60L * 1000L),
//                System.currentTimeMillis() + 2 * (24 * 60L * 60L * 1000L)
//        );

//        downloader.downloadExchangeRates(currencies, dates);


//        AutoResizeEditText view = (AutoResizeEditText) findViewById(R.id.test_view);
//        view.setCurrency(new Currency("AUD", "$", "Australian Dollar"));
    }

    @Override
    public void onDownloadComplete(List<ExchangeRate> exchangeRates) {
        for (ExchangeRate rate : exchangeRates) {
            Timber.v(rate.toString());
        }
    }
}
