package org.theronin.expensetracker.pages.entries.insert;

import android.support.annotation.NonNull;

import org.theronin.expensetracker.data.Contract.CategoryView;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceCategory;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.pages.entries.insert.CategorySelectAdapter.CategorySelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategorySelectPresenter implements CategorySelectedListener {

    private final AbsDataSource<Category> dataSourceCategory;
    private final CategorySelectUI categorySelectUI;

    private boolean mergeMode;
    private String mergeFromCategory;

    public CategorySelectPresenter(AbsDataSource<Category> dataSourceCategory,

                                   CategorySelectUI categorySelectUI) {
        this.dataSourceCategory = dataSourceCategory;
        this.categorySelectUI = categorySelectUI;
    }

    public void onNameChange(String oldCategoryName, String newCategoryName) {
        if (newCategoryName == null || newCategoryName.length() == 0) {
            categorySelectUI.showCategoryEmptyCategoryNameError();
            return;
        }
        if (oldCategoryName == null) {
            onCategoryCreated(newCategoryName);
        } else {
            onCategoryUpdated(oldCategoryName, newCategoryName);
        }
    }

    private void onCategoryCreated(String categoryName) {
        String sanitisedCategoryName = sanitiseCategoryName(categoryName);
        long id = dataSourceCategory.insert(new Category(sanitisedCategoryName)).getId();

        if (id == -1) {
            categorySelectUI.showCategoryDuplicateError(categoryName);
            return;
        }

        categorySelectUI.showCategoryCreationSuccess();
        categorySelectUI.returnCategoryResult(sanitisedCategoryName);
    }

    private void onCategoryUpdated(String oldCategoryName, String newCategoryName) {
        Category category = getCategory(sanitiseCategoryName(oldCategoryName));
        category.setName(sanitiseCategoryName(newCategoryName));
        if (!dataSourceCategory.update(category)) {
            categorySelectUI.showCategoryDuplicateError(newCategoryName);
        } else {
            categorySelectUI.showCategoryUpdateSuccess();
        }
    }

    @NonNull
    private Category getCategory(String sanitisedOldCategoryName) {
        List<Category> categories = dataSourceCategory.query(CategoryView.COL_CATEGORY_NAME + " = ?",
                new String[]{sanitisedOldCategoryName}, null);

        if (categories.size() != 1) {
            throw new IllegalStateException("The requested old category should exist, and there should only be 1");
        }
        return categories.get(0);
    }

    public void onCategoryDeleted(String categoryName) {
        dataSourceCategory.delete(getCategory(sanitiseCategoryName(categoryName)));
    }

    public void onCreateCategoryButtonSelected() {
        categorySelectUI.displayCategoryCreateDialog();
    }

    @Override
    public void onCategorySelected(Category category) {
        if (mergeMode) {
            finishMerge(category.getName());
        } else {
            categorySelectUI.returnCategoryResult(category.getName());
        }
    }

    @Override
    public void onMoreButtonSelected(Category category) {
        categorySelectUI.displayCategoryOptionsDialog(category);
    }

    private String sanitiseCategoryName(String categoryName) {
        categoryName = categoryName.toLowerCase();
        categoryName = categoryName.trim();
        return categoryName;
    }

    public void onBackButtonPressed() {
        if (inMergeMode()) {
            cancelMerge();
        } else {
            categorySelectUI.returnCategoryResult(null);
        }
    }

    public void startMerge(String categoryName) {
        this.mergeMode = true;
        this.mergeFromCategory = categoryName;
        categorySelectUI.setMergeCancelButtonVisible(true);
        categorySelectUI.setMergeHeaderVisible(mergeFromCategory, true);
        categorySelectUI.setCreateButtonVisible(false);
        categorySelectUI.setMoreOptionsVisible(false);
        categorySelectUI.setMergingCategoryHighlighted(categoryName);
    }

    public boolean inMergeMode() {
        return mergeMode;
    }

    public void finishMerge(String categoryName) {
        Category from = getCategory(sanitiseCategoryName(mergeFromCategory));
        Category to = getCategory(sanitiseCategoryName(categoryName));

        ((DataSourceCategory) dataSourceCategory).mergeCategories(new ArrayList<>(Arrays.asList(from)), to);
        cancelMerge();
    }

    public void cancelMerge() {
        this.mergeMode = false;
        this.mergeFromCategory = null;
        categorySelectUI.setMergeCancelButtonVisible(false);
        categorySelectUI.setMergeHeaderVisible(null, false);
        categorySelectUI.setCreateButtonVisible(true);
        categorySelectUI.setMoreOptionsVisible(true);
        categorySelectUI.setMergingCategoryHighlighted(null);
    }

    public interface CategorySelectUI {
        void showCategoryDuplicateError(String categoryName);

        void showCategoryCreationSuccess();

        void showCategoryUpdateSuccess();

        void showCategoryEmptyCategoryNameError();

        void returnCategoryResult(String categoryName);

        void displayCategoryCreateDialog();

        void displayCategoryOptionsDialog(Category category);

        void setMergeHeaderVisible(String categoryName, boolean visible);

        void setMergeCancelButtonVisible(boolean visible);

        void setCreateButtonVisible(boolean visible);

        void setMoreOptionsVisible(boolean visible);

        void setMergingCategoryHighlighted(String categoryName);
    }
}
