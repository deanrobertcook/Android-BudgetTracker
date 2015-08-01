package com.theronin.budgettracker.pages.categories;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.theronin.budgettracker.BudgetTrackerApplication;
import com.theronin.budgettracker.R;
import com.theronin.budgettracker.model.Category;

public class CategoriesActivity extends AppCompatActivity implements AddCategoryFragment.Container {

    private CategoryListFragment categoryListFragment;
    private AddCategoryFragment addCategoryFragment;
    private BudgetTrackerApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__categories);

        application = (BudgetTrackerApplication) getApplication();

        categoryListFragment = (CategoryListFragment) getFragmentManager()
                .findFragmentById(R.id.fragment__category_list);

        addCategoryFragment = (AddCategoryFragment) getFragmentManager()
                .findFragmentById(R.id.fragment__add_category);

    }

    @Override
    protected void onResume() {
        super.onResume();
        application.getCategoryStore().addObserver(categoryListFragment);
        application.getCategoryStore().notifyObservers();
    }

    @Override
    protected void onPause() {
        application.getCategoryStore().removeObserver(categoryListFragment);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        categoryListFragment = null;
        addCategoryFragment = null;
        application = null;
        super.onDestroy();
    }

    @Override
    public void onCategoryAdded(String categoryName) {
        if (categoryName.length() > 0) {
            try {
                application.getCategoryStore().addCategory(new Category(-1, categoryName, null));
                Toast.makeText(this, "Category successfully added", Toast.LENGTH_SHORT).show();
                addCategoryFragment.clearCategoryEditText();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Make sure that the category doesn't already exist",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "You must specify a name for the new category", Toast
                    .LENGTH_SHORT).show();
        }
    }
}
