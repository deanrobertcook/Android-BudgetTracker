package org.theronin.expensetracker.data.source;

import android.content.Context;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.loader.CategoryLoader;
import org.theronin.expensetracker.data.loader.EntryLoader;
import org.theronin.expensetracker.data.loader.ExchangeRateDownloadService;
import org.theronin.expensetracker.data.sync.SyncAdapter;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.pages.entries.EntryDialogActivity;
import org.theronin.expensetracker.pages.entries.EntryListFragment;
import org.theronin.expensetracker.pages.main.MainActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(injects = {
        MainActivity.class,
        EntryListFragment.class,
        EntryDialogActivity.class,
        SyncAdapter.class,
        DataSourceEntry.class,
        ExchangeRateDownloadService.class,
        EntryLoader.class,
        CategoryLoader.class
})
public class DataSourceModule {

    private InjectedComponent component;
    private Context context;
    private DbHelper dbHelper;

    public DataSourceModule(Context context, InjectedComponent component, DbHelper dbHelper) {
        this.component = component;
        this.context = context.getApplicationContext();
        this.dbHelper = dbHelper;
    }

    @Provides
    @Singleton
    AbsDataSource<Currency> provideCurrencyDataSource() {
        return new DataSourceCurrency(context, dbHelper);
    }

    @Provides
    @Singleton
    AbsDataSource<ExchangeRate> provideExchangeRateDataSource() {
        return new DataSourceExchangeRate(context, dbHelper);
    }

    @Provides
    @Singleton
    AbsDataSource<Category> provideCategoryDataSource() {
        return new DataSourceCategory(context, dbHelper);
    }

    @Provides
    @Singleton
    AbsDataSource<Entry> provideEntryDataSource() {
        return new DataSourceEntry(context, component, dbHelper);
    }
}
