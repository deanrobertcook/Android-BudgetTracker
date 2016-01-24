package org.theronin.expensetracker.data.backend.exchangerate;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DebugUtils;

import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import timber.log.Timber;

import static org.theronin.expensetracker.data.Contract.EntryView.COL_CURRENCY_CODE;

/**
 * Makes sure that there is an ExchangeRate for every entry in the database that has a Currency
 * different from the HomeCurrency. If it can't find such an exchange rate, it attempts a best
 * guess by looking for another date.
 */
public class ExchangeRateSyncCoordinator implements
        ExchangeRateDownloader.Callback {

    protected static final int MAX_DOWNLOAD_ATTEMPTS = 3;

    protected static final long BACKOFF_FIRST_ATTEMPT = 24L * 60L * 60L * 1000L; //a day
    protected static final long BACKOFF_SECOND_ATTEMPT = 7L * 24L * 60L * 60L * 1000L; //a week

    private final AbsDataSource<Entry> entryAbsDataSource;
    private final AbsDataSource<ExchangeRate> exchangeRateAbsDataSource;
    private final ExchangeRateDownloader downloader;
    private final Currency homeCurrency;

    /**
     * CodeDatePair to be downloaded by this instance of Sync Coordinator. They should be maintained
     * in reverse date order
     */
    private Set<ExchangeRate> ratesToDownload;

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

    public void downloadExchangeRates() {
        findPotentialExchangeRatesToDownload();
        filterOutPreviouslyDownloadedRates();
        if (ratesToDownload.isEmpty()) {
            return;
        }
        downloader.downloadExchangeRates(getDates(), getCodes());
    }

    /**
     * Returns a set of Dates as storage formatted strings from the current set of rates being
     * downloaded. Do not call this method before calling setRatesBeingDownloaded();
     *
     * @return the set of dates currently being downloaded.
     */
    private Set<String> getDates() {
        NavigableSet<String> dates = new TreeSet<>();
        for (ExchangeRate rate : ratesToDownload) {
            dates.add(rate.date);
        }
        return dates.descendingSet();
    }

    /**
     * Returns a set of currency codes as strings from the current set of rates being downloaded.
     * Do not call this method before calling setRatesBeingDownloaded
     *
     * @return the set of dates currently being downloaded.
     */
    private Set<String> getCodes() {
        Set<String> codes = new HashSet<>();
        for (ExchangeRate rate : ratesToDownload) {
            codes.add(rate.currencyCode);
        }
        return codes;
    }

    private void findPotentialExchangeRatesToDownload() {
        List<Entry> entries = entryAbsDataSource.query(COL_CURRENCY_CODE + " != ?", new String[]{homeCurrency.code}, null);
        ratesToDownload = new TreeSet<>();
        for (Entry entry : entries) {
            ratesToDownload.add(new ExchangeRate(entry.currency.code, entry.utcDate));
            ratesToDownload.add(new ExchangeRate(homeCurrency.code, entry.utcDate));
        }
    }

    private void filterOutPreviouslyDownloadedRates() {
        for (ExchangeRate rate : exchangeRateAbsDataSource.query()) {
            //take out the rateInfo to examine it.
            ratesToDownload.remove(rate);
            if (rate.getDownloadAttempts() == 0) {
                //successfully downloaded some time previously, don't try to download again
                //already removed, don't bother doing anything
            } else if (rate.getDownloadAttempts() >= MAX_DOWNLOAD_ATTEMPTS){
                //TODO deal with troublesome exchange rates!!
            } else {
                long timeSinceLastAttempt = System.currentTimeMillis() - rate.getUtcLastUpdated();

                if (rate.getDownloadAttempts() == 1 && timeSinceLastAttempt < BACKOFF_FIRST_ATTEMPT) {
                    continue;
                }

                if (rate.getDownloadAttempts() == 2 && timeSinceLastAttempt < BACKOFF_SECOND_ATTEMPT) {
                    continue;
                }
                ratesToDownload.add(rate);
            }
        }
    }

    @Override
    public void onDownloadComplete(List<ExchangeRate> downloadedRates) {
        if (ratesToDownload == null) {
            throw new IllegalStateException("onDownLoadComplete was called without calling" +
                    " downloadExchangeRates() first.");
        }
        DebugUtils.printList("Downloaded rates: ", downloadedRates);

        filterOutSuccessfullyDownloadedRates(downloadedRates);

        Timber.i(getDates().size() + " Exchange rates were not downloaded");

        downloadedRates.addAll(createFailedRates());
        exchangeRateAbsDataSource.bulkInsert(downloadedRates);
        ratesToDownload = null;
    }

    private void filterOutSuccessfullyDownloadedRates(List<ExchangeRate> downloadedRates) {
        for (ExchangeRate downloadedRate : downloadedRates) {
            ratesToDownload.remove(downloadedRate);
        }
    }

    private Set<ExchangeRate> createFailedRates() {
        for (ExchangeRate rate : ratesToDownload) {
            rate.onDownloadFailed();
        }
        return ratesToDownload;
    }
}
