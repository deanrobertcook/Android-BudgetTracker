package org.theronin.budgettracker.data.loader;

import android.app.Activity;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.model.Currency;

import java.util.List;

public class CurrencyLoader extends DataLoader<Currency> {

    public CurrencyLoader(Activity activity,
                          String selection,
                          String[] selectionArgs,
                          String orderBy) {
        super(activity,
                ((BudgetTrackerApplication) activity.getApplication()).getDataSourceCurrency(),
                selection, selectionArgs, orderBy
        );
    }

    @Override
    public List<Currency> loadInBackground() {
        return dataSource.query(selection, selectionArgs, orderBy);
    }
}
