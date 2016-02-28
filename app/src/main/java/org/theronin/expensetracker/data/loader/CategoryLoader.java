package org.theronin.expensetracker.data.loader;

import android.content.Context;

import org.theronin.expensetracker.data.Contract.CategoryView;
import org.theronin.expensetracker.data.Contract.EntryView;
import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.data.source.DataManager;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.NullCategory;

import java.util.Iterator;
import java.util.List;

public class CategoryLoader extends DataLoader<Category> implements AbsDataSource.Observer {

    private boolean calculateTotals;

    private AbsDataSource<Category> categoryDataSource;
    private AbsDataSource<Entry> entryDataSource;

    public CategoryLoader(Context context, boolean calculateTotals) {
        super(context);

        categoryDataSource = DataManager.getInstance().getDataSourceCategory();
        entryDataSource = DataManager.getInstance().getDataSourceEntry();
        setObservedDataSources(categoryDataSource, entryDataSource);
        this.calculateTotals = calculateTotals;
    }

    @Override
    public List<Category> loadInBackground() {
        List<Category> categories = categoryDataSource.query(
                CategoryView.COL_CATEGORY_NAME + " != ?", new String[] {NullCategory.NAME}, null);
        if (!calculateTotals) {
            return categories;
        } else {
            //TODO move this particular query into its own method
            List<Entry> allEntries = entryDataSource.query(
                    EntryView.COL_SYNC_STATUS + " NOT IN (" + SyncState.deleteStateSelection() + ")", null, null);
            assignHomeAmountsToEntries(allEntries);

            calculateTotals(categories, allEntries);
            return categories;
        }
    }

    protected void calculateTotals(List<Category> allCategories, List<Entry> allEntries) {
        for (Category category : allCategories) {
            long categoryTotal = 0;
            int missingEntries = 0;
            Iterator<Entry> entryIterator = allEntries.iterator();
            while (entryIterator.hasNext()) {
                Entry entry = entryIterator.next();
                if (category.equals(entry.getCategory())) {
                    entryIterator.remove();
                    if (entry.getHomeAmount() == -1) {
                        //TODO could have a more elegant way of handling missing entry rate data
                        //But for now I'll just drop them from the calculation
                        missingEntries++;
                    } else {
                        categoryTotal += entry.getHomeAmount();
                    }
                }
            }
            category.setTotal(categoryTotal);
            category.setMissingEntries(missingEntries);
        }
    }
}
