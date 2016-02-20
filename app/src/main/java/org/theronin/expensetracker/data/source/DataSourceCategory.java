package org.theronin.expensetracker.data.source;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang.NotImplementedException;
import org.theronin.expensetracker.data.Contract.CategoryTable;
import org.theronin.expensetracker.data.Contract.CategoryView;
import org.theronin.expensetracker.model.Category;

import java.util.Collection;
import java.util.List;

import timber.log.Timber;

import static org.theronin.expensetracker.data.Contract.CategoryTable.COL_NAME;
import static org.theronin.expensetracker.data.Contract.CategoryTable.TABLE_NAME;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_CATEGORY_NAME;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_ENTRY_FREQUENCY;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_FIRST_ENTRY_DATE;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_ID;

public class DataSourceCategory extends AbsDataSource<Category> {

    public DataSourceCategory(Context context, DbHelper dbHelper) {
        super(context, dbHelper);
        Timber.d("Instantiating DataSourceCategory");
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

    @Override
    public long getId(Category category) {
        long id = super.getId(category);

        if (id == -1) {
            return insert(category).getId();
        }
        return id;
    }

    @Override
    protected List<Category> searchForIdFromEntity(Category category) {
        return query(
                COL_NAME + " = ?",
                new String[]{category.getName()},
                null
        );
    }

    @Override
    protected long insertOperation(SQLiteDatabase db, Category category) {
        ContentValues values = getContentValues(category);
        return db.insertOrThrow(TABLE_NAME, null, values);
    }

    @Override
    protected int updateOperation(SQLiteDatabase db, Category category) {
        ContentValues values = getContentValues(category);
        return db.update(CategoryTable.TABLE_NAME,
                values,
                CategoryTable._ID + " = ?",
                new String[]{Long.toString(category.getId())});
    }

    @Override
    protected int deleteOperation(SQLiteDatabase sb, Collection<Category> entities) {
        throw new NotImplementedException("Can't yet delete Categories");
    }

    @Override
    protected ContentValues getContentValues(Category category) {
        ContentValues values = new ContentValues();
        if (category.getId() > -1) {
            values.put(CategoryTable._ID, category.getId());
        }
        values.put(CategoryTable.COL_NAME, category.getName());
        return values;
    }
}
