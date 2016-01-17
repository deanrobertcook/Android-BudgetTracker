package org.theronin.expensetracker.data.backend;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;
import org.theronin.expensetracker.utils.DebugUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

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

    /* Map from code to set of dates */
    private Map<String, Set<String>> ratesToDownload;

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
        if (ratesToDownload == null) {
            throw new IllegalStateException("onDownLoadComplete was called without calling" +
                    " downloadExchangeRates() first.");
        }
        DebugUtils.printList("Downloaded rates: ", downloadedRates);

        //TODO create a failed Exchange rate for each leftover date, and save them. At the moment
        //TODO they'll just be ignored, and any future attempts will try to download them again.
        for (ExchangeRate downloaded : downloadedRates) {
            if (ratesToDownload.containsKey(downloaded.currencyCode)) {
                ratesToDownload.get(downloaded.currencyCode).remove(
                        DateUtils.getStorageFormattedDate(downloaded.utcDate));
                if (ratesToDownload.get(downloaded.currencyCode).isEmpty()) {
                    ratesToDownload.remove(downloaded.currencyCode);
                }
            }
        }

        Timber.i(getDates() + " Exchange rates were not downloaded");

        exchangeRateAbsDataSource.bulkInsert(downloadedRates);
        ratesToDownload = null;
    }

    public void downloadExchangeRates() {
        findPotentialExchangeRatesToDownload();
        removeAlreadyDownloadedExchangeRates();
        if (ratesToDownload.isEmpty()) {
            return;
        }
        downloader.downloadExchangeRates(getDates(), ratesToDownload.keySet());
    }

    private Set<String> getDates() {
        Set<String> dates = new HashSet<>();
        for (String code : ratesToDownload.keySet()) {
            dates.addAll(ratesToDownload.get(code));
        }
        return dates;
    }

    private void findPotentialExchangeRatesToDownload() {

        List<Entry> entries = entryAbsDataSource.query(COL_CURRENCY_CODE + " != ?", new String[] {homeCurrency.code}, null);
        DebugUtils.printList("Potential entries: ", entries);
        ratesToDownload = new HashMap<>();
        for (Entry entry : entries) {
            String date = DateUtils.getStorageFormattedDate(entry.utcDate);

            Set<String> datesCurrent = ratesToDownload.get(entry.currency.code);
            datesCurrent = datesCurrent == null ? new HashSet<String>() : datesCurrent;
            datesCurrent.add(date);
            ratesToDownload.put(entry.currency.code, datesCurrent);

            Set<String> datesHome = ratesToDownload.get(homeCurrency.code);
            datesHome = datesHome == null ? new HashSet<String>() : datesHome;
            datesHome.add(date);
            ratesToDownload.put(homeCurrency.code, datesHome);
        }
    }

    private void removeAlreadyDownloadedExchangeRates() {
        List<ExchangeRate> alreadyDownloaded = exchangeRateAbsDataSource.query();
        DebugUtils.printList("Already downloaded exRates: ", alreadyDownloaded);

        for (ExchangeRate downloadedRate : alreadyDownloaded) {
            String date = DateUtils.getStorageFormattedDate(downloadedRate.utcDate);
            if (ratesToDownload.containsKey(downloadedRate.currencyCode)) {
                ratesToDownload.get(downloadedRate.currencyCode).remove(date);
                if (ratesToDownload.get(downloadedRate.currencyCode).isEmpty()) {
                    ratesToDownload.remove(downloadedRate.currencyCode);
                }
            }
        }
    }
}
