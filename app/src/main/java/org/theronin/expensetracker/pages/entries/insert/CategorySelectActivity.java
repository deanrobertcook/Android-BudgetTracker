package org.theronin.expensetracker.pages.entries.insert;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.dagger.InjectedActivity;
import org.theronin.expensetracker.data.loader.CategoryLoader;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.pages.categories.CategoryDialogFragment;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class CategorySelectActivity extends InjectedActivity implements
        LoaderManager.LoaderCallbacks<List<Category>>,
        CategorySelectAdapter.CategorySelectedListener,
        View.OnClickListener,
        CategoryDialogFragment.Container {

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
        toolbar.setTitle("Select a Category");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab__add_category_button);
        floatingActionButton.setOnClickListener(this);

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
        returnCategoryResult(categoryName);
    }

    private void returnCategoryResult(String categoryName) {
        Intent result = new Intent(RESULT_ACTION);
        result.putExtra(CATEGORY_NAME_KEY, categoryName);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab__add_category_button:
                Timber.d("FAB clicked");
                new CategoryDialogFragment().show(getFragmentManager(), CategoryDialogFragment.TAG);
                break;
        }
    }

    @Override
    public void onCategoryCreated(String categoryName) {
        if (categoryName != null && categoryName.length() > 0) {
            String sanitisedCategoryName = sanitiseCategoryName(categoryName);
            long id = dataSourceCategory.insert(new Category(sanitisedCategoryName)).getId();
            if (id == -1) {
                Toast.makeText(this, R.string.duplicate_category_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.category_creation_success, Toast.LENGTH_SHORT).show();
                returnCategoryResult(sanitisedCategoryName);
            }
        } else {
            Toast.makeText(this, R.string.empty_category_name_error, Toast.LENGTH_SHORT).show();
        }
    }

    private String sanitiseCategoryName(String categoryName) {
        categoryName = categoryName.toLowerCase();
        categoryName = categoryName.trim();
        return categoryName;
    }
}
