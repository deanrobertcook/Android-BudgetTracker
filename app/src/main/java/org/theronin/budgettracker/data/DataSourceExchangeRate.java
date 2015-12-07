package org.theronin.budgettracker.data;

import org.theronin.budgettracker.BudgetTrackerApplication;
import org.theronin.budgettracker.model.ExchangeRate;

import java.util.List;

public class DataSourceExchangeRate extends DataSource<ExchangeRate> {

    public DataSourceExchangeRate(BudgetTrackerApplication application) {
        super(application);
    }

    @Override
    public int bulkInsert(List<ExchangeRate> entities) {
        return super.bulkInsert(entities);
    }
}
