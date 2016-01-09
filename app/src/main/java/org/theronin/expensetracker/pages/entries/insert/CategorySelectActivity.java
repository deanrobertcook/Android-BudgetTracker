package org.theronin.expensetracker.pages.entries.insert;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.dagger.InjectedActivity;
import org.theronin.expensetracker.data.loader.CategoryLoader;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.pages.entries.insert.CategorySelectAdapter;

import java.util.List;

import javax.inject.Inject;

public class CategorySelectActivity extends InjectedActivity implements
        LoaderManager.LoaderCallbacks<List<Category>>,CategorySelectAdapter.CategorySelectedListener {

    public static final String CATEGORY_NAME_KEY = "CATEGORY_NAME";
    public static final String RESULT_ACTION = "org.theronin.expensetracker.CATEGORY_SELECTED";
    private static final int CATEGORY_LOADER_ID = 1;

    @Inject AbsDataSource<Category> dataSourceCategory;

    private CategorySelectAdapter selectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__category_select);

        getLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);

        selectAdapter = new CategorySelectAdapter(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view__category_list);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(selectAdapter);
    }

    @Override
    public Loader<List<Category>> onCreateLoader(int id, Bundle args) {
        return new CategoryLoader(this, this, false);
    }

    @Override
    public void onLoadFinished(Loader<List<Category>> loader, List<Category> data) {
        selectAdapter.setCategories(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Category>> loader) {
        selectAdapter.setCategories(null);
    }

    @Override
    public void onCategorySelected(String categoryName) {
        Intent result = new Intent(RESULT_ACTION);
        result.putExtra(CATEGORY_NAME_KEY, categoryName);
        setResult(RESULT_OK, result);
        finish();
    }
}
