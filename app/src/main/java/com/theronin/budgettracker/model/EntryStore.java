package com.theronin.budgettracker.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.theronin.budgettracker.BudgetTrackerApplication;
import com.theronin.budgettracker.data.BudgetContract.EntriesTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import timber.log.Timber;

public class EntryStore {

    private BudgetTrackerApplication application;
    private HashSet<Observer> observers;
    private List<Entry> entries;

    public EntryStore(BudgetTrackerApplication application) {
        this.application = application;
        this.observers = new HashSet<>();
    }

    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.onEntriesLoaded(entries);
        }
    }

    public void fetchEntries() {
        Cursor cursor = application.getContentResolver().query(
                EntriesTable.CONTENT_URI,
                new String[]{
                        EntriesTable._ID,
                        EntriesTable.COL_CATEGORY_ID,
                        EntriesTable.COL_DATE_ENTERED,
                        EntriesTable.COL_AMOUNT_CENTS
                }, null, null, null);

        entries = new ArrayList<>();

        while (cursor.moveToNext()) {
            String categoryName = null;
            try {
                categoryName = application.getCategoryStore().findCategoryById(cursor.getLong(1)).name;
            } catch (Exception e) {
                e.printStackTrace();
            }

            Entry entry = new Entry(cursor.getLong(0), categoryName, cursor.getString(2), cursor.getLong(3));
            entries.add(entry);
        }

        notifyObservers();
    }


    public long addEntry(Entry entry) {
        ContentValues values = new ContentValues();

        long categoryId = -1;
        try {
            categoryId = application.getCategoryStore().findCategoryByName(entry.categoryName).id;
        } catch (Exception e) {
            Timber.e("Category couldn't be found");
            return -1;
        }

        values.put(EntriesTable.COL_CATEGORY_ID, categoryId);
        values.put(EntriesTable.COL_DATE_ENTERED, entry.dateEntered);
        values.put(EntriesTable.COL_AMOUNT_CENTS, entry.amount);

        Uri newEntryUri = application.getContentResolver().insert(EntriesTable.CONTENT_URI, values);
        long newRowId = Integer.parseInt(newEntryUri.getLastPathSegment());

        //Whenever an Entry is added, we need to update the category store to update the totals
        application.getCategoryStore().fetchCategories();
        fetchEntries();
        return newRowId;
    }

    public int deleteEntry(Entry entry) {
        int numDeleted = application.getContentResolver().delete(
                EntriesTable.CONTENT_URI.buildUpon().appendPath(Long.toString(entry.id)).build(),
                null, null
        );
        fetchEntries();
        return numDeleted;
    }

    public void tearDown() {
        application = null;
        observers = null;
        entries = null;
    }

    public interface Observer {
        void onEntriesLoaded(List<Entry> entries);
    }
}
