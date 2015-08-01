package com.theronin.budgettracker.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.theronin.budgettracker.BudgetTrackerApplication;
import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import com.theronin.budgettracker.data.BudgetContract.EntriesTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CategoryStore {

    private BudgetTrackerApplication application;
    private HashSet<Observer> observers;
    private List<Category> categories;

    public CategoryStore(BudgetTrackerApplication application) {
        this.application = application;
        this.observers = new HashSet<>();
    }

    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.onCategoriesLoaded(categories);
        }
    }

    public void fetchCategories() {
        Cursor categoriesCursor = queryCategories();
        categories = new ArrayList<>();

        while (categoriesCursor.moveToNext()) {

            Cursor entryAmountsCursor = queryEntryAmountsForCategory(categoriesCursor.getLong(0));
            long total = 0;
            long entryCount = 0;
            while (entryAmountsCursor.moveToNext()) {
                total += entryAmountsCursor.getLong(0);
                entryCount++;
            }

            Category category = new Category(
                    categoriesCursor.getLong(0),
                    categoriesCursor.getString(1),
                    categoriesCursor.getString(2),
                    total,
                    entryCount);

            categories.add(category);
        }

        notifyObservers();
    }

    private Cursor queryCategories() {
        return application.getContentResolver().query(
                CategoriesTable.CONTENT_URI,
                new String[]{
                        CategoriesTable._ID,
                        CategoriesTable.COL_CATEGORY_NAME,
                        CategoriesTable.COL_DATE_CREATED
                }, null, null, null);
    }

    private Cursor queryEntryAmountsForCategory(long categoryId) {
        return application.getContentResolver().query(
                EntriesTable.CONTENT_URI,
                new String[] {
                        EntriesTable.COL_AMOUNT_CENTS
                },
                EntriesTable.COL_CATEGORY_ID + " = ?",
                new String[] {
                    Long.toString(categoryId)
                },
                null
        );
    }

    public Category addCategory(Category category) throws Exception {
        ContentValues values = new ContentValues();
        values.put(CategoriesTable.COL_CATEGORY_NAME, category.name);

        if (category.date != null) {
            values.put(CategoriesTable.COL_DATE_CREATED, category.date);
        }

        Uri newCategoryURI = application.getContentResolver().insert(CategoriesTable.CONTENT_URI,
                values);
        long newRowId = Integer.parseInt(newCategoryURI.getLastPathSegment());
        if (newRowId == -1) {
            throw new Exception(application.getString(R.string.failed_to_add_category, category.name));
        }
        Category completedCategory = fetchCategory(newRowId);

        fetchCategories();
        return completedCategory;
    }

    private Category fetchCategory(long categoryId) {
        Cursor cursor = application.getContentResolver().query(CategoriesTable.CONTENT_URI,
                new String[]{
                        CategoriesTable._ID,
                        CategoriesTable.COL_CATEGORY_NAME,
                        CategoriesTable.COL_DATE_CREATED},
                CategoriesTable._ID + " = ?", new String[]{Long.toString(categoryId)},
                null);

        cursor.moveToFirst();
        return new Category(cursor.getLong(0), cursor.getString(1), cursor.getString(0));
    }

    public Category findCategoryByName(String categoryName) throws Exception {
        for (Category category : categories) {
            if (category.name.equals(categoryName)) {
                return category;
            }
        }
        throw new Exception(application.getString(R.string.exception_category_not_found_by_name, categoryName));
    }

    public Category findCategoryById(long id) throws Exception {
        for (Category category : categories) {
            if (category.id == id) {
                return category;
            }
        }
        throw new Exception(application.getString(R.string.exception_category_not_found_by_id, id));
    }

    public void tearDown() {
        application = null;
        observers = null;
        categories = null;
    }


    public interface Observer {
        public void onCategoriesLoaded(List<Category> categories);
    }
}
