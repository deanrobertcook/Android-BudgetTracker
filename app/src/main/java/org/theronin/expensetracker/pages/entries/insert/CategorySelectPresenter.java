package org.theronin.expensetracker.pages.entries.insert;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataSourceCategory;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.pages.entries.insert.CategorySelectAdapter.CategorySelectedListener;
import org.theronin.expensetracker.utils.TrackingUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class CategorySelectPresenter implements CategorySelectedListener {

    private final AbsDataSource<Category> dataSourceCategory;
    private final CategorySelectUI categorySelectUI;

    private boolean mergeMode;
    private Category mergeFrom;
    private Category mergeTo;

    public CategorySelectPresenter(AbsDataSource<Category> dataSourceCategory,
                                   CategorySelectUI categorySelectUI) {
        this.dataSourceCategory = dataSourceCategory;
        this.categorySelectUI = categorySelectUI;
    }

    public void onNameChange(Category category, String newCategoryName) {
        if (newCategoryName == null || newCategoryName.length() == 0) {
            categorySelectUI.showCategoryEmptyCategoryNameError();
            return;
        }
        if (category == null) {
            onCategoryCreated(newCategoryName);
        } else {
            onCategoryUpdated(category, newCategoryName);
        }
    }

    private void onCategoryCreated(String categoryName) {
        Category category = new Category(categoryName);
        TrackingUtils.categoryCreated(category);
        long id = dataSourceCategory.insert(category).getId();

        if (id == -1) {
            categorySelectUI.showCategoryDuplicateError(category);
            return;
        }

        categorySelectUI.showCategoryCreationSuccess();
        categorySelectUI.returnCategoryResult(category);
    }

    private void onCategoryUpdated(Category category, String newCategoryName) {
        TrackingUtils.categoryUpdated(category, newCategoryName);
        category.setName(newCategoryName);
        if (!dataSourceCategory.update(category)) {
            categorySelectUI.showCategoryDuplicateError(category);
        } else {
            categorySelectUI.showCategoryUpdateSuccess();
        }
    }

    public void onCategoryDeleted(Category category) {
        TrackingUtils.categoryDeleted(category);
        dataSourceCategory.delete(category);
    }

    public void onCreateCategoryButtonSelected() {
        categorySelectUI.displayCategoryCreateDialog();
    }

    @Override
    public void onCategorySelected(Category category) {
        if (mergeMode) {
            if (category.equals(mergeFrom)) {
                return; //do nothing when same category is clicked
            }
            this.mergeTo = category;
            categorySelectUI.showConfirmMergeDialog(mergeFrom, mergeTo);
        } else {
            categorySelectUI.returnCategoryResult(category);
        }
    }

    @Override
    public void onMoreButtonSelected(Category category) {
        categorySelectUI.displayCategoryOptionsDialog(category);
    }

    public void onBackButtonPressed() {
        if (inMergeMode()) {
            cancelMerge();
        } else {
            categorySelectUI.returnCategoryResult(null);
        }
    }

    public void startMerge(Category category) {
        this.mergeMode = true;
        this.mergeFrom = category;
        categorySelectUI.setMergeCancelButtonVisible(true);
        categorySelectUI.setMergeHeaderVisible(mergeFrom, true);
        categorySelectUI.setCreateButtonVisible(false);
        categorySelectUI.setMoreOptionsVisible(false);
        categorySelectUI.setMergingCategoryHighlighted(category);
    }

    public boolean inMergeMode() {
        return mergeMode;
    }

    public void finishMerge() {
        if (!mergeMode || mergeFrom == null || mergeTo == null) {
            throw new IllegalStateException("finishMerge should only be called after a merge is started and a category " +
                    "has been selected");
        }
        TrackingUtils.categoriesMerged(mergeFrom, mergeTo);
        ((DataSourceCategory) dataSourceCategory).mergeCategories(new ArrayList<>(Arrays.asList(mergeFrom)), mergeTo);
        cancelMerge();
    }

    public void cancelMerge() {
        this.mergeMode = false;
        this.mergeFrom = null;
        categorySelectUI.setMergeCancelButtonVisible(false);
        categorySelectUI.setMergeHeaderVisible(null, false);
        categorySelectUI.setCreateButtonVisible(true);
        categorySelectUI.setMoreOptionsVisible(true);
        categorySelectUI.setMergingCategoryHighlighted(null);
    }

    public interface CategorySelectUI {
        void showCategoryDuplicateError(Category category);

        void showCategoryCreationSuccess();

        void showCategoryUpdateSuccess();

        void showCategoryEmptyCategoryNameError();

        void returnCategoryResult(Category category);

        void displayCategoryCreateDialog();

        void displayCategoryOptionsDialog(Category category);

        void setMergeHeaderVisible(Category category, boolean visible);

        void setMergeCancelButtonVisible(boolean visible);

        void setCreateButtonVisible(boolean visible);

        void setMoreOptionsVisible(boolean visible);

        void setMergingCategoryHighlighted(Category category);

        void showConfirmMergeDialog(Category from, Category to);
    }
}
