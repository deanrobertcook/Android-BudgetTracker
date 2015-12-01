package org.theronin.budgettracker.comparators;

import org.theronin.budgettracker.data.BudgetContract.CategoriesView;
import org.theronin.budgettracker.model.Category;

import java.util.Comparator;

public class CategoryAlphabeticalComparator implements Comparator<Category> {

    public static final String SQL_SORT_ORDER = CategoriesView.COL_CATEGORY_NAME + " ASC";

    @Override
    public int compare(Category lhs, Category rhs) {
        return lhs.name.compareTo(rhs.name);
    }
}