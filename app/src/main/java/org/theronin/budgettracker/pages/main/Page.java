package org.theronin.budgettracker.pages.main;

import org.theronin.budgettracker.R;

public enum Page {
    ENTRIES("Entries", R.drawable.ic_entry_unselected, R.drawable.ic_entry_selected),
    CATEGORIES("Categories", R.drawable.ic_category_unselected, R.drawable.ic_category_selected);

    String title;
    int unselectedIconResId;
    int selectedIconResId;

    Page(String title, int unselectedIconResId, int selectedIconResId) {
        this.title = title;
        this.unselectedIconResId = unselectedIconResId;
        this.selectedIconResId = selectedIconResId;
    }
}
