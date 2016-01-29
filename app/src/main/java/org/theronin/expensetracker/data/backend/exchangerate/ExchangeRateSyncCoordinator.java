package org.theronin.expensetracker.data.backend.exchangerate;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DebugUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static org.theronin.expensetracker.data.Contract.EntryView.COL_CURRENCY_CODE;

/**
 * Makes sure that there is an ExchangeRate for every entry in the database that has a Currency
 * different from the HomeCurrency.
 */
public class ExchangeRateSyncCoordinator implements
        ExchangeRateDownloader.Callback {

    protected static final long BASE_BACKOFF = 60L * 1000L; // one minute.
    protected static final double BACKOFF_RATE = 2.0;

    private final AbsDataSource<Entry> entryAbsDataSource;
    private final AbsDataSource<ExchangeRate> exchangeRateAbsDataSource;
    private final ExchangeRateDownloader downloader;
    private final Currency homeCurrency;

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
     * downloaded.
     *
     * @return the set of dates currently being downloaded.
     */
    private Set<String> getDates() {
        Set<String> dates = new HashSet<>();
        for (ExchangeRate rate : ratesToDownload) {
            dates.add(rate.date);
        }
        return dates;
    }

    /**
     * Returns a set of currency codes as strings from the current set of rates being downloaded.
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
        ratesToDownload = new HashSet<>();
        for (Entry entry : entries) {
            ratesToDownload.add(new ExchangeRate(entry.currency.code, entry.utcDate));
            ratesToDownload.add(new ExchangeRate(homeCurrency.code, entry.utcDate));
        }
    }

    private void filterOutPreviouslyDownloadedRates() {
        for (ExchangeRate rate : exchangeRateAbsDataSource.query()) {
            //take out the rateInfo to examine it.
            ratesToDownload.remove(rate);
            if (rate.getDownloadAttempts() > 0 && isLastAttemptOutsideBackoffTime(rate)) {
                ratesToDownload.add(rate);
            }
        }
    }

    private boolean isLastAttemptOutsideBackoffTime(ExchangeRate rate) {
        long backOffTime = BASE_BACKOFF * (long) Math.pow(BACKOFF_RATE, rate.getDownloadAttempts() - 1);
        return rate.getUtcLastUpdated() < System.currentTimeMillis() - backOffTime;
    }

    @Override
    public void onDownloadComplete(List<ExchangeRate> downloadedRates) {
        if (ratesToDownload == null) {
            throw new IllegalStateException("onDownLoadComplete was called without calling" +
                    " downloadExchangeRates() first.");
        }
        Timber.d("Downloaded rates:");
        DebugUtils.printList(getClass().getSimpleName(), downloadedRates);

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

    //TODO do a test with an in-memory database. I'm worried that bulkInsert will just fail silently
    //TODO whenever we try to increment an ExchangeRate's download attempt data.
}
