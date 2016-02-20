package org.theronin.expensetracker.pages.entries.insert;

import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.pages.entries.insert.CategorySelectAdapter.CategorySelectedListener;

public class CategorySelectPresenter implements CategorySelectedListener {

    private final AbsDataSource<Category> dataSourceCategory;
    private final CategorySelectUI categorySelectUI;

    public CategorySelectPresenter(AbsDataSource<Category> dataSourceCategory,

                                   CategorySelectUI categorySelectUI) {
        this.dataSourceCategory = dataSourceCategory;
        this.categorySelectUI = categorySelectUI;
    }

    public void onCategoryCreated(String categoryName) {
        if (categoryName != null && categoryName.length() > 0) {
            String sanitisedCategoryName = sanitiseCategoryName(categoryName);
            long id = dataSourceCategory.insert(new Category(sanitisedCategoryName)).getId();
            if (id == -1) {
                categorySelectUI.showCategoryDuplicateError();
            } else {
                categorySelectUI.showCategoryCreationSuccess();
                categorySelectUI.returnCategoryResult(sanitisedCategoryName);
            }
        } else {
            categorySelectUI.showCategoryEmptyCategoryNameError();
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
        void showCategoryDuplicateError();

        void showCategoryCreationSuccess();

        void showCategoryEmptyCategoryNameError();

        void returnCategoryResult(String categoryName);

        void displayCategoryCreateDialog();

        void displayCategoryOptionsDialog(Category category);
    }
}
