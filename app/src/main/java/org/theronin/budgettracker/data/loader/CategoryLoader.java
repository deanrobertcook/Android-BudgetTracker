package org.theronin.budgettracker.data.loader;

import android.app.Activity;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.model.Category;

import java.util.List;

public class CategoryLoader extends DataLoader<Category> {

    public CategoryLoader(Activity activity,
                          String selection,
                          String[] selectionArgs,
                          String orderBy,
                          boolean calculateTotals) {
        super(activity,
                ((BudgetTrackerApplication) activity.getApplication()).getDataSourceCategory(),
                selection, selectionArgs, orderBy
        );
    }

    @Override
    public List<Category> loadInBackground() {
        return dataSource.query(selection, selectionArgs, orderBy);
    }
}
