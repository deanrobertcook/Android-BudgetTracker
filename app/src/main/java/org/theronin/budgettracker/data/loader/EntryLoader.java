package org.theronin.budgettracker.data.loader;

import android.app.Activity;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.model.Entry;

import java.util.List;

public class EntryLoader extends DataLoader<Entry> {

    public EntryLoader(Activity activity,
                       String selection,
                       String[] selectionArgs,
                       String orderBy) {
        super(activity,
                ((BudgetTrackerApplication) activity.getApplication()).getDataSourceEntry(),
                selection, selectionArgs, orderBy
        );
    }

    @Override
    public List<Entry> loadInBackground() {
        return dataSource.query(selection, selectionArgs, orderBy);
    }
}
