package org.theronin.budgettracker.comparators;

import org.theronin.budgettracker.model.Category;

import java.util.Comparator;

public class CategoryFrequencyComparator implements Comparator<Category> {
    @Override
    public int compare(Category lhs, Category rhs) {
        return (int) (lhs.frequency - rhs.frequency);
    }
}

