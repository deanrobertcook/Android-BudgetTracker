package org.theronin.expensetracker;

import android.content.Context;

import org.theronin.expensetracker.data.backend.entry.EntryRemoteSync;
import org.theronin.expensetracker.data.backend.entry.ParseEntryRemoteSync;
import org.theronin.expensetracker.data.backend.exchangerate.ExchangeRateDownloadService;
import org.theronin.expensetracker.data.backend.exchangerate.ExchangeRateDownloader;
import org.theronin.expensetracker.data.backend.exchangerate.ParseExchangeRateDownloader;
import org.theronin.expensetracker.data.loader.CategoryLoader;
import org.theronin.expensetracker.data.loader.DataLoader;
import org.theronin.expensetracker.data.loader.EntryLoader;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceCategory;
import org.theronin.expensetracker.data.source.DataSourceCurrency;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DataSourceExchangeRate;
import org.theronin.expensetracker.data.source.DbHelper;
import org.theronin.expensetracker.data.sync.SyncAdapter;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.pages.entries.insert.CategorySelectActivity;
import org.theronin.expensetracker.pages.entries.insert.EntryDialogActivity;
import org.theronin.expensetracker.pages.entries.list.EntryListFragment;
import org.theronin.expensetracker.pages.main.DebugActivity;
import org.theronin.expensetracker.pages.main.MainActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(injects = {
        MainActivity.class,
        DebugActivity.class,
        EntryListFragment.class,
        EntryDialogActivity.class,
        CategorySelectActivity.class,
        SyncAdapter.class,
        ExchangeRateDownloadService.class,
        EntryLoader.class,
        CategoryLoader.class,
        DataLoader.class
})
public class AppModule {

    private AbsDataSource<ExchangeRate> exchangeRateDataSource;
    private AbsDataSource<Category> categoryDataSource;
    private AbsDataSource<Entry> entryDataSource;

    public AppModule(Context context, DbHelper dbHelper) {
        this.exchangeRateDataSource = new DataSourceExchangeRate(context, dbHelper);
        this.categoryDataSource = new DataSourceCategory(context, dbHelper);
        AbsDataSource<Currency> currencyDataSource = new DataSourceCurrency(context, dbHelper);
        this.entryDataSource = new DataSourceEntry(context, dbHelper, categoryDataSource, currencyDataSource);
    }

    @Provides
    @Singleton
    AbsDataSource<ExchangeRate> provideExchangeRateDataSource() {
        return exchangeRateDataSource;
    }

    @Provides
    @Singleton
    AbsDataSource<Category> provideCategoryDataSource() {
        return categoryDataSource;
    }

    @Provides
    @Singleton
    AbsDataSource<Entry> provideEntryDataSource() {
        return entryDataSource;
    }

    @Provides
    EntryRemoteSync provideEntryRemoteSync() {
        return new ParseEntryRemoteSync();
    }

    @Provides
    ExchangeRateDownloader provideExchangeRateDownloader() {
        return new ParseExchangeRateDownloader();
    }
}
