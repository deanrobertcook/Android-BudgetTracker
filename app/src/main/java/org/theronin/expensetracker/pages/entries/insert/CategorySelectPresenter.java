package org.theronin.expensetracker.pages.entries.insert;

import org.theronin.expensetracker.data.Contract.CategoryView;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.pages.entries.insert.CategorySelectAdapter.CategorySelectedListener;

import java.util.List;

public class CategorySelectPresenter implements CategorySelectedListener {

    private final AbsDataSource<Category> dataSourceCategory;
    private final CategorySelectUI categorySelectUI;

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
        String sanitisedOldCategoryName = sanitiseCategoryName(oldCategoryName);
        String sanitisedNewCategoryName = sanitiseCategoryName(newCategoryName);

        List<Category> categories = dataSourceCategory.query(CategoryView.COL_CATEGORY_NAME + " = ?",
                new String[] {sanitisedOldCategoryName}, null);

        if (categories.size() != 1) {
            throw new IllegalStateException("The requested old category should exist, and there should only be 1");
        }

        Category category = categories.get(0);

        category.setName(sanitisedNewCategoryName);
        if (!dataSourceCategory.update(category)) {
            categorySelectUI.showCategoryDuplicateError(newCategoryName);
        } else {
            categorySelectUI.showCategoryUpdateSuccess();
        }
    }

    public void onCreateCategoryButtonSelected() {
        categorySelectUI.displayCategoryCreateDialog();
    }

    @Override
    public void onCategorySelected(Category category) {
        categorySelectUI.returnCategoryResult(category.getName());
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
        categorySelectUI.returnCategoryResult(null);
    }

    public interface CategorySelectUI {
        void showCategoryDuplicateError(String categoryName);

        void showCategoryCreationSuccess();

        void showCategoryUpdateSuccess();

        void showCategoryEmptyCategoryNameError();

        void returnCategoryResult(String categoryName);

        void displayCategoryCreateDialog();

        void displayCategoryOptionsDialog(Category category);
    }
}
