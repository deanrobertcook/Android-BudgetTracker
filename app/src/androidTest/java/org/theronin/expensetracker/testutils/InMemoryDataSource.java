package org.theronin.expensetracker.testutils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceCategory;
import org.theronin.expensetracker.data.source.DataSourceCurrency;
import org.theronin.expensetracker.data.source.DataSourceEntry;
import org.theronin.expensetracker.data.source.DataSourceExchangeRate;
import org.theronin.expensetracker.data.source.DbHelper;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;

public class InMemoryDataSource {

    private AbsDataSource<ExchangeRate> exchangeRateDataSource;
    private AbsDataSource<Category> categoryDataSource;
    private AbsDataSource<Entry> entryDataSource;

    public InMemoryDataSource() {
        Context context = InstrumentationRegistry.getTargetContext();
        DbHelper dbHelper = DbHelper.getInstance(context, null);

        this.exchangeRateDataSource = new DataSourceExchangeRate(context, dbHelper);
        this.categoryDataSource = new DataSourceCategory(context, dbHelper);
        AbsDataSource<Currency> currencyDataSource = new DataSourceCurrency(context, dbHelper);
        this.entryDataSource = new DataSourceEntry(context, dbHelper, categoryDataSource, currencyDataSource);
    }

    public AbsDataSource<Entry> getEntryDataSource() {
        return entryDataSource;
    }

    public AbsDataSource<ExchangeRate> getExchangeRateDataSource() {
        return exchangeRateDataSource;
    }

    public AbsDataSource<Category> getCategoryDataSource() {
        return categoryDataSource;
    }

}
