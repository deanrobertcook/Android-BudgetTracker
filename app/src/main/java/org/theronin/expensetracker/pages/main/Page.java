package org.theronin.expensetracker.pages.main;

import org.theronin.expensetracker.R;

public enum Page {
    ENTRIES(R.drawable.ic_entry_unselected, R.drawable.ic_entry_selected),
    CATEGORIES(R.drawable.ic_category_unselected, R.drawable.ic_category_selected);

    public String title;
    public int id;
    public int unselectedIconResId;
    public int selectedIconResId;

    Page(int unselectedIconResId, int selectedIconResId) {
        this.title = name().substring(0, 1) + name().substring(1).toLowerCase();
        this.id = ordinal();
        this.unselectedIconResId = unselectedIconResId;
        this.selectedIconResId = selectedIconResId;
    }

    public static Page valueOf(int id) {
        if (id < 0 || id >= Page.values().length) {
            return null;
        }
        return values()[id];
    }
}
