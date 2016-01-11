package org.theronin.expensetracker.data.backend;

import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;

import java.util.List;

public class ExchangeRateSyncCoordinator implements ExchangeRateDownloader.Callback {

    public ExchangeRateDownloader downloader;

    public ExchangeRateSyncCoordinator(ExchangeRateDownloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public void onDownloadComplete(List<ExchangeRate> exchangeRates) {

    }

    public void findExchangeRates(List<Entry> entries) {

    }

}
