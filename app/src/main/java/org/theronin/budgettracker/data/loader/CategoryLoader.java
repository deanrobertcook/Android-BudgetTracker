package org.theronin.budgettracker.data.loader;

import android.app.Activity;
import android.content.AsyncTaskLoader;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.AbsDataSource;
import org.theronin.budgettracker.data.DataSourceCategory;
import org.theronin.budgettracker.data.DataSourceEntry;
import org.theronin.budgettracker.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryLoader extends AsyncTaskLoader<List<Category>> implements AbsDataSource.Observer {

    private DataSourceCategory dataSourceCategory;
    private DataSourceEntry dataSourceEntry;

    private String selection;
    private String[] selectionArgs;
    private String orderBy;

    private List<Category> categories;

    private boolean calculateTotals;

    public CategoryLoader(Activity activity,
                          String selection,
                          String[] selectionArgs,
                          String orderBy,
                          boolean calculateTotals) {
        super(activity);
        BudgetTrackerApplication application = (BudgetTrackerApplication) activity.getApplication();
        dataSourceCategory = application.getDataSourceCategory();
        dataSourceEntry = application.getDataSourceEntry();

        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.orderBy = orderBy;
        this.calculateTotals = calculateTotals;
    }

    @Override
    public List<Category> loadInBackground() {
        if (!calculateTotals) {
            return dataSourceCategory.query(selection, selectionArgs, orderBy);
        } else {


            return new ArrayList<>();
        }
    }

    @Override
    public void deliverResult(List<Category> data) {
        this.categories = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (categories != null) {
            deliverResult(categories);
        }

        dataSourceCategory.registerObserver(this);
        dataSourceEntry.registerObserver(this);

        if (takeContentChanged() || categories == null || categories.isEmpty()) {
            forceLoad();
        }
    }

    @Override
    public void onDataSourceChanged() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        dataSourceCategory.unregisterObserver(this);
        dataSourceEntry.unregisterObserver(this);
    }

    //    private void calculateTotals() {
//        if (entryCursor == null || !entryCursor.moveToFirst()) {
//            Timber.d("Entry cursor null or empty!");
//            return;
//        }
//
//        entryCursor.moveToPosition(-1);
//        totals = new HashMap<>();
//        missingEntries = 0;
//
//        while (entryCursor.moveToNext()) {
//            final Entry entry = Entry.fromCursor(entryCursor);
//            if (entry.currency.code.equals(homeCurrency.code)) {
//                Timber.d("Entry currency is the same as home currency");
//                addEntryAmountToTotal(entry);
//            } else {
//                if (entry.rate.usdRate == -1) {
//                    Timber.d("Entry rate not available: " + entry.toString());
//                    new ExchangeRateDownloadAgent().getExchangeData(context, entry.utcDateEntered);
//                    missingEntries++;
//                } else {
//                    if (homeCurrency.getExchangeRate(entry.utcDateEntered) == null) {
//                        Timber.d("Still missing home currency data");
//                        missingEntries++;
//                    } else {
//                        Timber.d("Performing currency conversion calculation");
//                        addEntryAmountToTotal(entry);
//                    }
//                }
//            }
//        }
//    }
//
//    private void addEntryAmountToTotal(Entry entry) {
//        long currentTotal;
//        if (totals.get(entry.category.id) == null) {
//            currentTotal = 0;
//        } else {
//            currentTotal = totals.get(entry.category.id);
//        }
//        currentTotal += calculateAmountInHomeCurrency(entry);
//        totals.put(entry.category.id, currentTotal);
//    }
//
//    private long calculateAmountInHomeCurrency(Entry entry) {
//        if (entry.currency.code.equals(homeCurrency.code)) {
//            return entry.amount;
//        } else {
//            double directExchangeRate =
//                    homeCurrency.getExchangeRate(entry.utcDateEntered).usdRate / entry.rate.usdRate;
//            Timber.d("directExchangeRate: " + directExchangeRate);
//            long convertedEntryAmount = (long) (directExchangeRate * (double) entry.amount);
//            Timber.d("convertedEntryAmount: " + convertedEntryAmount);
//            return convertedEntryAmount;
//        }
//    }
}
