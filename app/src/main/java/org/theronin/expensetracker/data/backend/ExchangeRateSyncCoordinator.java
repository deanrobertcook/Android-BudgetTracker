package org.theronin.expensetracker.data.backend;

import android.support.annotation.NonNull;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;
import org.theronin.expensetracker.utils.DebugUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

    protected static final int DEFAULT_DOWNLOAD_BATCH_SIZE = 100;

    private int downloadBatchSize = DEFAULT_DOWNLOAD_BATCH_SIZE;

    private final AbsDataSource<Entry> entryAbsDataSource;
    private final AbsDataSource<ExchangeRate> exchangeRateAbsDataSource;
    private final ExchangeRateDownloader downloader;
    private final Currency homeCurrency;

    /**
     * CodeDatePair to be downloaded by this instance of Sync Coordinator. They should be maintained
     * in reverse date order
     */
    private Set<CodeDatePair> ratesToDownload;

    /**
     * The next/current set of rates to be downloaded (represented by CodeDatePairs), as many of which
     * will be taken from ratesToDownload up to the download threshold limit
     */
    private Set<CodeDatePair> ratesBeingDownloaded;

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

    public void setDownloadBatchSize(int batchSize) {
        this.downloadBatchSize = batchSize;
    }

    public void downloadExchangeRates() {
        findPotentialExchangeRatesToDownload();
        removeDownloadedExchangeRatesFrom(exchangeRateAbsDataSource.query(), ratesToDownload);
        if (ratesToDownload.isEmpty()) {
            return;
        }
        downloadNextBatch();
    }

    private void downloadNextBatch() {
        setRatesBeingDownloaded();
        downloader.downloadExchangeRates(getDates(), getCodes());
    }

    /**
     * This method creates a Set of CodeRatePairs by removing as many CodeRatePairs from the
     * ratesToDownload set as possible up to the DOWNLOAD_THRESHOLD. This new set then becomes
     * the rates currently being processed by the downloader/coordinator. This should be called
     * before each call to the downloader's downloadExchangeRates() method.
     */
    private void setRatesBeingDownloaded() {
        Set<CodeDatePair> rates = new HashSet<>();
        int i = 0;
        Iterator<CodeDatePair> iterator = ratesToDownload.iterator();
        while (iterator.hasNext()) {
            if (i == downloadBatchSize) {
                break;
            }
            CodeDatePair next = iterator.next();
            iterator.remove();
            rates.add(next);
            i++;
        }
        ratesBeingDownloaded = rates;
    }

    /**
     * Returns a set of Dates as storage formatted strings from the current set of rates being
     * downloaded. Do not call this method before calling setRatesBeingDownloaded();
     *
     * @return the set of dates currently being downloaded.
     */
    private Set<String> getDates() {
        Set<String> dates = new HashSet<>();
        for (CodeDatePair rate : ratesBeingDownloaded) {
            dates.add(rate.date);
        }
        return dates;
    }

    /**
     * Returns a set of currency codes as strings from the current set of rates being downloaded.
     * Do not call this method before calling setRatesBeingDownloaded
     *
     * @return the set of dates currently being downloaded.
     */
    private Set<String> getCodes() {
        Set<String> codes = new HashSet<>();
        for (CodeDatePair rate : ratesBeingDownloaded) {
            codes.add(rate.code);
        }
        return codes;
    }

    private void findPotentialExchangeRatesToDownload() {
        List<Entry> entries = entryAbsDataSource.query(COL_CURRENCY_CODE + " != ?", new String[]{homeCurrency.code}, null);
        ratesToDownload = new TreeSet<>();
        for (Entry entry : entries) {
            ratesToDownload.add(new CodeDatePair(entry.currency.code, entry.utcDate));
            ratesToDownload.add(new CodeDatePair(homeCurrency.code, entry.utcDate));
        }
    }

    /**
     * Removes all code-date combinations from the some set of CodeRatePairs.
     *
     * @param exchangeRates A list of exchange rates (their codes/dates) to remove
     * @param ratePairs     The set of CodeRatePairs to remove the downloaded exchange rates from.
     */
    private void removeDownloadedExchangeRatesFrom(List<ExchangeRate> exchangeRates, Set<CodeDatePair> ratePairs) {
        for (ExchangeRate downloadedRate : exchangeRates) {
            ratePairs.remove(new CodeDatePair(downloadedRate));
        }
    }

    @Override
    public void onDownloadComplete(List<ExchangeRate> downloadedRates) {
        if (ratesToDownload == null) {
            throw new IllegalStateException("onDownLoadComplete was called without calling" +
                    " downloadExchangeRates() first.");
        }
        DebugUtils.printList("Downloaded rates: ", downloadedRates);

        removeDownloadedExchangeRatesFrom(downloadedRates, ratesBeingDownloaded);

        //TODO create a failed Exchange rate for each leftover date, and save them. At the moment
        //TODO they'll just be ignored, and any future attempts will try to download them again.
        Timber.i(getDates().size() + " Exchange rates were not downloaded");

        exchangeRateAbsDataSource.bulkInsert(downloadedRates);

        //finished downloading this batch.
        ratesBeingDownloaded = null;

        if (!ratesToDownload.isEmpty()) {
            downloadNextBatch();
        } else {
            //finished downloading all rates
            ratesToDownload = null;
        }
    }

    protected static class CodeDatePair implements Comparable<CodeDatePair> {
        public final String code;
        public final String date;

        public CodeDatePair(String code, String date) {
            if (code.length() != 3 || date.length() != 10) {
                throw new IllegalArgumentException("Be sure that the code and date are in the right format");
            }
            this.code = code;
            this.date = date;
        }

        public CodeDatePair(String code, long date) {
            this(code, DateUtils.getStorageFormattedDate(date));
        }

        public CodeDatePair(ExchangeRate exchangeRate) {
            this(exchangeRate.currencyCode, exchangeRate.utcDate);
        }

        @Override
        public String toString() {
            return String.format("[%s, %s]", code, date);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CodeDatePair)) {
                return false;
            }
            CodeDatePair other = (CodeDatePair) o;
            return code.equals(other.code) && date.equals(other.date);
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(@NonNull CodeDatePair another) {
            int byDate = -date.compareTo(another.date);
            if (byDate == 0) {
                return code.compareTo(another.code);
            }
            return byDate;
        }
    }
}
