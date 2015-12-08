package org.theronin.budgettracker.comparators;

import org.theronin.budgettracker.model.Category;

import java.util.Comparator;

public class CategoryTotalComparator implements Comparator<Category> {

    @Override
    public int compare(Category lhs, Category rhs) {
        return (int) -(lhs.getTotal() - rhs.getTotal());
    }
}
