package org.theronin.expensetracker.data.backend;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DebugUtils;
import org.theronin.expensetracker.utils.ExchangeRateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.theronin.expensetracker.data.Contract.EntryView.COL_CURRENCY_CODE;

/**
 * Makes sure that there is an ExchangeRate for every entry in the database that has a Currency
 * different from the HomeCurrency. If it can't find such an exchange rate, it attempts a best
 * guess by looking for another date.
 */
public class ExchangeRateSyncCoordinator implements
        ExchangeRateDownloader.Callback {

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

        this.downloader.setCallback(this);
    }

    @Override
    public void onDownloadComplete(List<ExchangeRate> downloadedRates) {
        if (ratesBeingDownloaded == null) {
            throw new IllegalStateException("Download was called without a data source change");
        }

        DebugUtils.printList("Downloaded rates: ", downloadedRates);

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
                waiting.setUsdRate(downloaded.getUsdRate());
                i++;
            }
        }

        DebugUtils.printList("ratesBeingDownloaded - final cut: ", ratesBeingDownloaded);

        exchangeRateAbsDataSource.bulkInsert(ratesBeingDownloaded);
        ratesBeingDownloaded = null;
    }

    public void downloadExchangeRates() {
        findPotentialExchangeRatesToDownload();
        removeAlreadyDownloadedExchangeRates();
        if (ratesBeingDownloaded.isEmpty()) {
            return;
        }
        downloader.downloadExchangeRates(ratesBeingDownloaded);
    }

    private void findPotentialExchangeRatesToDownload() {
        List<Entry> entries = entryAbsDataSource.query(COL_CURRENCY_CODE + " != ?", new String[] {homeCurrency.code}, null);
        DebugUtils.printList("Potential entries: ", entries);
        ratesBeingDownloaded = new ArrayList<>();
        for (Entry entry : entries) {
            ExchangeRate currentRate = new ExchangeRate(-1, entry.currency.code, entry.utcDate, -1, -1, 0);
            ExchangeRate homeRate = new ExchangeRate(-1, homeCurrency.code, entry.utcDate, -1, -1, 0);

            if (!containsExchangeRate(ratesBeingDownloaded, currentRate)) {
                ratesBeingDownloaded.add(currentRate);
            }

            if (!containsExchangeRate(ratesBeingDownloaded, homeRate)) {
                ratesBeingDownloaded.add(homeRate);
            }
        }
    }

    private boolean containsExchangeRate(List<ExchangeRate> rates, ExchangeRate rateToCheck) {
        for (ExchangeRate rate : rates) {
            if (rate.equals(rateToCheck)) {
                return true;
            }
        }
        return false;
    }

    private void removeAlreadyDownloadedExchangeRates() {
        List<ExchangeRate> alreadyDownloaded = exchangeRateAbsDataSource.query();
        DebugUtils.printList("Already downloaded exRates: ", alreadyDownloaded);
        Iterator<ExchangeRate> iterator = ratesBeingDownloaded.iterator();
        while (iterator.hasNext()) {
            ExchangeRate rateToDownload = iterator.next();
            for (ExchangeRate downloadedRate : alreadyDownloaded) {
                if (rateToDownload.equals(downloadedRate)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }
}
