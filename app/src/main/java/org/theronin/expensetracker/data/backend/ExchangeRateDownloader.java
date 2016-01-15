package org.theronin.expensetracker.data.backend;

import org.theronin.expensetracker.model.ExchangeRate;

import java.util.List;

public interface ExchangeRateDownloader {

    interface Callback {
        void onDownloadComplete(List<ExchangeRate> exchangeRates);
    }

    void setCallback(Callback callback);

    void downloadExchangeRates(List<ExchangeRate> ratesToDownload);
}
