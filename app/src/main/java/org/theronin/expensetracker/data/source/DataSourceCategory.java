package org.theronin.expensetracker.data.source;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.expensetracker.data.Contract.CategoryTable;
import org.theronin.expensetracker.data.Contract.CategoryView;
import org.theronin.expensetracker.model.Category;

import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

import static org.theronin.expensetracker.data.Contract.CategoryTable.COL_NAME;
import static org.theronin.expensetracker.data.Contract.CategoryTable.TABLE_NAME;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_CATEGORY_NAME;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_ENTRY_FREQUENCY;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_FIRST_ENTRY_DATE;
import static org.theronin.expensetracker.data.Contract.CategoryView.INDEX_ID;

public class DataSourceCategory extends AbsDataSource<Category> {

    /**
     * Useful for when another datasource needs to know about changes in this datasource.
     */
    protected UpdateListener listener;

    protected void setListener(UpdateListener listener) {
        this.listener = listener;
    }

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
        assertListenerNotNull();
        ContentValues values = getContentValues(category);

        int affected = db.update(CategoryTable.TABLE_NAME,
                values,
                CategoryTable._ID + " = ?",
                new String[]{Long.toString(category.getId())});

        if (affected != 0) {
            listener.onCategoryEdited(category);
        }

        return affected;
    }

    @Override
    protected int deleteOperation(SQLiteDatabase db, List<Category> entities) {
        assertListenerNotNull();
        if (entities.size() > 1) {
            throw new IllegalArgumentException("Bulk deletion not supported by category data source");
        }

        listener.beforeCategoryDeleted(entities.get(0));

        return db.delete(CategoryTable.TABLE_NAME,
                CategoryTable._ID  + " = ?",
                new String[] {Long.toString(entities.get(0).getId())});
    }

    private void assertListenerNotNull() {
        if (listener == null) {
            throw new IllegalStateException("The Category DataSource needs to inform other data sources on updates");
        }
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

    public void mergeCategories(List<Category> from, Category to) {
        assertListenerNotNull();
        Iterator<Category> iterator = from.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(to)) {
                iterator.remove();
            }
        }

        if (from.size() == 0) {
            return;
        }

        listener.beforeCategoryMerged(from, to);

        for (Category category : from) {
            delete(category);
        }
        setDataInValid();
    }

    /**
     * An interface for other data sources only
     * TODO this feels a bit hacky. It might be better to think of a BUS system instead
     */
    protected interface UpdateListener {
        void onCategoryEdited(Category category);

        void beforeCategoryDeleted(Category category);

        void beforeCategoryMerged(List<Category> from, Category to);
    }
}
