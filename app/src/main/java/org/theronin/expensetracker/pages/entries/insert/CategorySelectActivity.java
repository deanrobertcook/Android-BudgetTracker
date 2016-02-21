package org.theronin.expensetracker.pages.entries.insert;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.dagger.InjectedActivity;
import org.theronin.expensetracker.data.loader.CategoryLoader;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.pages.entries.insert.dialogs.CategoryNameEditFragment;
import org.theronin.expensetracker.pages.entries.insert.dialogs.CategoryOptionsDialogFragment;

import java.util.List;

import javax.inject.Inject;

public class CategorySelectActivity extends InjectedActivity implements
        LoaderManager.LoaderCallbacks<List<Category>>,
        CategorySelectPresenter.CategorySelectUI,
        CategoryNameEditFragment.Container,
        CategoryOptionsDialogFragment.Container,
        View.OnClickListener {

    public static final String CATEGORY_KEY = "CATEGORY";
    public static final String RESULT_ACTION = "org.theronin.expensetracker.CATEGORY_SELECTED";
    private static final int CATEGORY_LOADER_ID = 1;

    @Inject AbsDataSource<Category> dataSourceCategory;

    private CategorySelectAdapter selectAdapter;
    private CategorySelectPresenter presenter;

    private Drawable defaultHomeButtonDrawable;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__category_select);

        getLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
        toolbar.setTitle(R.string.select_category_dialog_header);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        defaultHomeButtonDrawable = toolbar.getNavigationIcon();

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab__add_category_button);
        floatingActionButton.setOnClickListener(this);

        presenter = new CategorySelectPresenter(dataSourceCategory, this);
        selectAdapter = new CategorySelectAdapter(this, presenter);

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab__add_category_button:
                presenter.onCreateCategoryButtonSelected();
                break;
        }
    }

    @Override
    public void showCategoryDuplicateError(Category category) {
        Toast.makeText(this, getString(R.string.duplicate_category_error, category.getDisplayName()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showCategoryCreationSuccess() {
        Toast.makeText(this, R.string.category_creation_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showCategoryUpdateSuccess() {
        Toast.makeText(this, R.string.category_update_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showCategoryEmptyCategoryNameError() {
        Toast.makeText(this, R.string.empty_category_name_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void returnCategoryResult(Category category) {
        if (category != null) {
            Intent result = new Intent(RESULT_ACTION);
            result.putExtra(CATEGORY_KEY, category);
            setResult(RESULT_OK, result);
        }
        finish();
        overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_out);
    }

    @Override
    public void displayCategoryCreateDialog() {
        CategoryNameEditFragment.newInstance(null).show(getFragmentManager(), CategoryNameEditFragment.TAG);
    }

    @Override
    public void displayCategoryOptionsDialog(Category category) {
       CategoryOptionsDialogFragment.newInstance(category)
               .show(getFragmentManager(), CategoryOptionsDialogFragment.TAG);
    }

    @Override
    public void setMergeHeaderVisible(Category category, boolean visible) {
        if (visible) {
            getSupportActionBar().setTitle(getString(R.string.merging_categories, category.getDisplayName()));
        } else {
            getSupportActionBar().setTitle(R.string.select_category_dialog_header);
        }
    }

    @Override
    public void setMergeCancelButtonVisible(boolean visible) {
        if (visible) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear);
        } else {
            getSupportActionBar().setHomeAsUpIndicator(defaultHomeButtonDrawable);
        }
    }

    @Override
    public void setCreateButtonVisible(boolean visible) {
        if (visible) {
            floatingActionButton.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void setMoreOptionsVisible(boolean visible) {
        selectAdapter.setMoreButtonsVisible(visible);
    }

    @Override
    public void setMergingCategoryHighlighted(Category category) {
        selectAdapter.setCategoryHighlighted(category);
    }

    @Override
    public void showConfirmMergeDialog(Category from, Category to) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.category_merge_confirm_dialog__title)
                .setMessage(getString(R.string.category_merge_confirm_dialog__message, from.getDisplayName(), to.getDisplayName()))
                .setPositiveButton(R.string.category_merge_confirm_dialog__positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.finishMerge();
                    }
                })
                .setNegativeButton(R.string.category_merge_confirm_dialog__negative_button, null)
                .show();
    }

    @Override
    public void onPositiveButtonClicked(Category category, String newCategoryName) {
        presenter.onNameChange(category, newCategoryName);
    }

    @Override
    public void onBackPressed() {
        presenter.onBackButtonPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            presenter.onBackButtonPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onEditClicked(Category category) {
        CategoryNameEditFragment.newInstance(category).show(getFragmentManager(), CategoryNameEditFragment.TAG);
    }

    @Override
    public void onMergeClicked(final Category category) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.category_merge_dialog__title, category.getDisplayName()))
                .setMessage(R.string.category_merge_dialog__message)
                .setPositiveButton(R.string.category_merge_dialog__positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.startMerge(category);
                    }
                })
                .setNegativeButton(R.string.category_merge_dialog__negative_button, null)
                .show();
    }

    @Override
    public void onDeleteClicked(final Category category) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.category_delete_dialog__title, category.getDisplayName()))
                .setMessage(R.string.category_delete_dialog__message)
                .setPositiveButton(R.string.category_delete_dialog__positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.onCategoryDeleted(category);
                    }
                })
                .setNegativeButton(R.string.category_delete_dialog__negative_button, null)
                .show();
    }
}
