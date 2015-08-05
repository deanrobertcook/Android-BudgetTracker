package com.theronin.budgettracker.pages.categories;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetContract;
import com.theronin.budgettracker.data.BudgetContract.CategoriesTable;
import com.theronin.budgettracker.model.Category;

import timber.log.Timber;

public class CategoriesActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, AddCategoryFragment.Container {

    private CategoryListFragment categoryListFragment;
    private AddCategoryFragment addCategoryFragment;

    public static final int CATEGORY_LOADER_ID = 0;
    private Loader<Cursor> loader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__categories);

        categoryListFragment = (CategoryListFragment) getFragmentManager()
                .findFragmentById(R.id.fragment__category_list);

        addCategoryFragment = (AddCategoryFragment) getFragmentManager()
                .findFragmentById(R.id.fragment__add_category);

        loader = getLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        categoryListFragment = null;
        addCategoryFragment = null;
        super.onDestroy();
    }

    @Override
    public void onCategoryAdded(String categoryName) {
        if (categoryName == null || categoryName.equals("")) {
            Toast.makeText(this, "You must specify a name for the new category", Toast
                    .LENGTH_SHORT).show();
        }

        ContentValues values = new ContentValues();
        values.put(CategoriesTable.COL_CATEGORY_NAME, categoryName);

        Uri categoryUri = getContentResolver().insert(
                CategoriesTable.CONTENT_URI,
                values
        );

        long id = Long.parseLong(categoryUri.getLastPathSegment());

        if (id == -1) {
            Toast.makeText(this, "Make sure that the category doesn't already exist",
                    Toast.LENGTH_SHORT).show();
        } else {
            if (loader != null) {
                loader.onContentChanged();
            }

            addCategoryFragment.clearCategoryEditText();

            Toast.makeText(this, "Category added succesfully",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //////////////////////////////
    // Loader Callbacks
    //////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("onCreateLoader");
        return new CursorLoader(
                this,
                BudgetContract.CategoriesTable.CONTENT_URI,
                Category.projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (categoryListFragment != null) {
            categoryListFragment.updateAdapter(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (categoryListFragment != null) {
            categoryListFragment.updateAdapter(null);
        }
    }
}
