package org.theronin.expensetracker.data.backend;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.ExchangeRateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.theronin.expensetracker.data.Contract.EntryView.COL_CURRENCY_CODE;

/**
 * Makes sure that there is an ExchangeRate for every entry in the database that has a Currency
 * different from the HomeCurrency. If it can't find such an exchange rate, it attempts a best
 * guess by looking for another date.
 */
public class ExchangeRateSyncCoordinator implements
        ExchangeRateDownloader.Callback,
        AbsDataSource.Observer {

    private final AbsDataSource<Entry> entryAbsDataSource;
    private final AbsDataSource<ExchangeRate> exchangeRateAbsDataSource;
    private final ExchangeRateDownloader downloader;
    private final Currency homeCurrency;

    private List<ExchangeRate> ratesBeingDownloaded;

    public ExchangeRateSyncCoordinator(AbsDataSource<Entry> entryAbsDataSource,
                                       AbsDataSource<ExchangeRate> exchangeRateAbsDataSource,
                                       ExchangeRateDownloader downloader,
                                       Currency homeCurrency) {
        this.entryAbsDataSource = entryAbsDataSource;
        this.exchangeRateAbsDataSource = exchangeRateAbsDataSource;
        this.downloader = downloader;
        this.homeCurrency = homeCurrency;

        entryAbsDataSource.registerObserver(this);
    }

    protected String getQueryString() {
        return COL_CURRENCY_CODE + " != ?";
    }

    @Override
    public void onDownloadComplete(List<ExchangeRate> downloadedRates) {
        if (ratesBeingDownloaded == null) {
            throw new IllegalStateException("Download was called without a data source change");
        }
        Collections.sort(ratesBeingDownloaded, ExchangeRateUtils.comparator());
        Collections.sort(downloadedRates, ExchangeRateUtils.comparator());

        int i = 0;
        for (ExchangeRate waiting : ratesBeingDownloaded) {
            ExchangeRate downloaded = null;
            if (i < downloadedRates.size()) {
                downloaded = downloadedRates.get(i);
            }
            if (!waiting.equals(downloaded)) {
                waiting.onDownloadFailed();
            } else {
                i++;
            }
        }

        exchangeRateAbsDataSource.bulkInsert(ratesBeingDownloaded);
        ratesBeingDownloaded = null;
    }

    @Override
    public void onDataSourceChanged() {
        List<Entry> entries = entryAbsDataSource.query(getQueryString(), new String[] {homeCurrency.code}, null);
        if (entries.isEmpty()) {
            return;
        }

        ratesBeingDownloaded = new ArrayList<>();

        for (Entry entry : entries) {
            ratesBeingDownloaded.add(new ExchangeRate(-1, entry.currency.code, entry.utcDate, -1, -1, 0));
            ratesBeingDownloaded.add(new ExchangeRate(-1, homeCurrency.code, entry.utcDate, -1, -1, 0));
        }

        downloader.downloadExchangeRates(ratesBeingDownloaded);
    }
}
