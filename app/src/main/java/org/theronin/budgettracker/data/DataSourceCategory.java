package org.theronin.budgettracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.data.BudgetContract.CategoriesView;
import org.theronin.budgettracker.model.Category;

import java.util.ArrayList;
import java.util.List;

public class DataSourceCategory extends AbsDataSource<Category> {

    public DataSourceCategory(BudgetTrackerApplication application) {
        super(application);
    }

    public long getId(String categoryName) {
        List<Category> categories = query(
                BudgetContract.CategoriesTable.COL_CATEGORY_NAME + " = ?",
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
        long categoryId = db.insert(BudgetContract.CategoriesTable.TABLE_NAME, null, values);
        setDataInValid();
        return categoryId;
    }

    @Override
    public List<Category> query(String selection, String[] selectionArgs, String orderBy) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                CategoriesView.VIEW_NAME,
                CategoriesView.PROJECTION,
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
}
