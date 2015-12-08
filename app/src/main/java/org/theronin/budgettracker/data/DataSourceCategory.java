package org.theronin.budgettracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.BudgetContract.CategoryView;
import org.theronin.budgettracker.model.Category;

import java.util.ArrayList;
import java.util.List;

public class DataSourceCategory extends AbsDataSource<Category> {

    public DataSourceCategory(BudgetTrackerApplication application) {
        super(application);
    }

    public long getId(String categoryName) {
        List<Category> categories = query(
                BudgetContract.CategoryTable.COL_NAME + " = ?",
                new String[]{categoryName},
                null
        );
        if (categories.isEmpty()) {
            return insert(new Category(categoryName));
        }
        return categories.get(0).id;
    }

    @Override
    public long insert(Category entity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = entity.toValues();
        long categoryId = db.insert(BudgetContract.CategoryTable.TABLE_NAME, null, values);
        setDataInValid();
        return categoryId;
    }

    @Override
    public List<Category> query(String selection, String[] selectionArgs, String orderBy) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                CategoryView.VIEW_NAME,
                CategoryView.PROJECTION,
                selection,
                selectionArgs,
                null, null, orderBy
        );

        List<Category> categories = new ArrayList<>();
        while (cursor.moveToNext()) {
            categories.add(Category.fromCursor(cursor));
        }
        cursor.close();
        return categories;
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
