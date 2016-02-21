package org.theronin.expensetracker.pages.main;

import org.apache.commons.lang.WordUtils;
import org.theronin.expensetracker.R;

public enum MainPage {
    MANAGE_EXPENSES(R.drawable.ic_entry_unselected, R.drawable.ic_entry_selected),
    OVERVIEW(R.drawable.ic_category_unselected, R.drawable.ic_category_selected);

    public String title;
    public int id;
    public int unselectedIconResId;
    public int selectedIconResId;

    MainPage(int unselectedIconResId, int selectedIconResId) {
        this.title = WordUtils.capitalize(name().replace("_", " ").toLowerCase());
        this.id = ordinal();
        this.unselectedIconResId = unselectedIconResId;
        this.selectedIconResId = selectedIconResId;
    }

    public static MainPage valueOf(int id) {
        if (id < 0 || id >= MainPage.values().length) {
            return null;
        }
        return values()[id];
    }
}
