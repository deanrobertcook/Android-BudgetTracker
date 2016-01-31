package org.theronin.expensetracker.data.loader;

import android.content.Context;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.source.AbsDataSource;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Entry;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

public class CategoryLoader extends DataLoader<Category> implements AbsDataSource.Observer {

    private boolean calculateTotals;

    @Inject AbsDataSource<Category> categoryDataSource;
    @Inject AbsDataSource<Entry> entryDataSource;

    public CategoryLoader(Context context, InjectedComponent component, boolean calculateTotals) {
        super(context, component);
        setObservedDataSources(categoryDataSource, entryDataSource);
        this.calculateTotals = calculateTotals;
    }

    @Override
    public List<Category> loadInBackground() {
        List<Category> categories = categoryDataSource.query();
        if (!calculateTotals) {
            return categories;
        } else {
            List<Entry> allEntries = entryDataSource.query();
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
                if (category.name.equals(entry.category.name)) {
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
