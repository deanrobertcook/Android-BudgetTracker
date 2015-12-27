package org.theronin.expensetracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang.NotImplementedException;
import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.data.Contract.CategoryView;
import org.theronin.expensetracker.model.Category;

import java.util.Collection;
import java.util.List;

import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_CATEGORY_NAME;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_ENTRY_FREQUENCY;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_FIRST_ENTRY_DATE;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_ID;

public class DataSourceCategory extends AbsDataSource<Category> {

    public DataSourceCategory(CustomApplication application) {
        super(application);
    }

    @Override
    protected String getTableName() {
        return CategoryView.VIEW_NAME;
    }

    @Override
    protected String[] getQueryProjection() {
        return CategoryView.PROJECTION;
    }

    @Override
    protected Category fromCursor(Cursor cursor) {
        long id = cursor.getLong(INDEX_ID);
        String categoryName = cursor.getString(INDEX_CATEGORY_NAME);
        long utcDateFirstEntered = cursor.getLong(INDEX_FIRST_ENTRY_DATE);
        long entryFrequency = cursor.getLong(INDEX_ENTRY_FREQUENCY);

        return new Category(id, categoryName, utcDateFirstEntered, entryFrequency);
    }

    public long getId(String categoryName) {
        List<Category> categories = query(
                Contract.CategoryTable.COL_NAME + " = ?",
                new String[]{categoryName},
                null
        );
        if (categories.isEmpty()) {
            return insert(new Category(categoryName));
        }
        return categories.get(0).getId();
    }

    @Override
    protected long insertOperation(SQLiteDatabase db, Category category) {
        ContentValues values = category.toValues();
        return db.insert(Contract.CategoryTable.TABLE_NAME, null, values);
    }

    @Override
    protected int updateOperation(SQLiteDatabase db, Category entity) {
        throw new NotImplementedException("Category update method not yet implemented");
    }

    @Override
    protected int deleteOperation(SQLiteDatabase sb, Collection<Category> entities) {
        throw new NotImplementedException("Can't yet delete Categories");
    }
}
