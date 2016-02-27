package org.theronin.expensetracker.utils;

import com.localytics.android.Localytics;
import com.parse.ParseUser;

import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Entry;

import java.util.HashMap;
import java.util.Map;

public class TrackingUtils {

    /**
     * For the default user
     * @param user
     */
    public static void setUserDetails(String user) {
        Localytics.setCustomerId(user);
    }

    public static void setUserDetails(ParseUser user) {
        Localytics.setCustomerId(user.getObjectId());
        Localytics.setCustomerEmail(user.getEmail());
    }

    public static void addEntryDialogOpened() {
        Localytics.tagEvent("Entry Dialog Opened");
    }

    public static void extraInputRowCreated() {
        Localytics.tagEvent("Extra Input Row Created");
    }

    public static void categoryFieldClicked() {
        Localytics.tagEvent("Category Field Clicked");
    }

    public static void categoryDeleted(Category category) {
        Localytics.tagEvent("Category Deleted", mapCategory(category));
    }

    public static void categoryCreated(Category category) {
        Localytics.tagEvent("Category Created", mapCategory(category));
    }

    public static void categoryUpdated(Category category, String newName) {
        Localytics.tagEvent("Category Updated", mapCategory(category, newName));
    }

    public static void categoriesMerged(Category from, Category to) {
        Localytics.tagEvent("Category Updated", mergedCategoryValues(from, to));
    }

    public static void entryCreated(Entry entry) {
        Localytics.tagEvent("Entry Created", mapEntry(entry));
    }

    private static Map<String, String> mapCategory(Category category) {
        return mapCategory(category, null);
    }

    private static Map<String, String> mapCategory(Category category, String newName) {
        Map<String, String> values = new HashMap<>();
        values.put("Name", category.getName());
        if (newName != null) {
            values.put("New Name", newName);
        }
        if (category.frequency > 0) {
            values.put("Frequency", Long.toString(category.frequency));
        }
        if (category.getTotal() > 0) {
            values.put("Total", Long.toString(category.getTotal()));
        }
        return values;
    }

    //TODO refactor this out
    private static Map<String, String> mergedCategoryValues(Category from, Category to) {
        Map<String, String> values = new HashMap<>();

        values.put("From Name", from.getName());
        if (from.frequency > 0) {
            values.put("From Frequency", Long.toString(from.frequency));
        }
        if (from.getTotal() > 0) {
            values.put("From Total", Long.toString(from.getTotal()));
        }

        values.put("To Name", to.getName());
        if (to.frequency > 0) {
            values.put("To Frequency", Long.toString(to.frequency));
        }
        if (to.getTotal() > 0) {
            values.put("To Total", Long.toString(to.getTotal()));
        }
        return values;
    }

    private static Map<String, String> mapEntry(Entry entry) {
        Map<String, String> values = new HashMap<>();
        values.put("Amount", Long.toString(entry.amount));
        values.put("Category", entry.getCategory().getName());
        values.put("Currency", entry.currency.code);
        values.put("Date", DateUtils.getDisplayFormattedDate(entry.utcDate));
        return values;
    }
}
