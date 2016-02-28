package org.theronin.expensetracker.data.source;

import android.content.Context;

import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.SyncUtils;

public class DataManager {

    private static DataManager instance;

    private final Context context;
    private DbHelper helper;
    private AbsDataSource<Entry> dataSourceEntry;
    private AbsDataSource<Category> dataSourceCategory;
    private AbsDataSource<ExchangeRate> dataSourceExchangeRate;
    private AbsDataSource<Currency> dataSourceCurrency;

    public static void setup(Context context, DbHelper helper) {
        context = context.getApplicationContext();
        if (instance == null) {
            instance = new DataManager(context, helper);
        }
        if (instance.helper != helper) {
            instance.onDatabaseChanged(helper);
        }
    }

    public static DataManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("The DataManager has not yet been created");
        }
        return instance;
    }

    private DataManager(Context context, DbHelper helper) {
        this.context = context;
        this.helper = helper;
        dataSourceCategory = new DataSourceCategory(context, helper);
        dataSourceExchangeRate = new DataSourceExchangeRate(context, helper);
        dataSourceCurrency = new DataSourceCurrency(context, helper);
        dataSourceEntry = new DataSourceEntry(
                context,
                helper,
                dataSourceCategory,
                dataSourceCurrency
        );
    }

    private void onDatabaseChanged(DbHelper newHelper) {
        this.helper = newHelper;
        instance.dataSourceEntry.setDbHelper(helper);
        instance.dataSourceCategory.setDbHelper(helper);
        instance.dataSourceExchangeRate.setDbHelper(helper);
        instance.dataSourceCurrency.setDbHelper(helper);
        SyncUtils.requestSync(context);
    }

    public AbsDataSource<Entry> getDataSourceEntry() {
        return dataSourceEntry;
    }

    public AbsDataSource<Category> getDataSourceCategory() {
        return dataSourceCategory;
    }

    public AbsDataSource<ExchangeRate> getDataSourceExchangeRate() {
        return dataSourceExchangeRate;
    }

    public AbsDataSource<Currency> getDataSourceCurrency() {
        return dataSourceCurrency;
    }
}
