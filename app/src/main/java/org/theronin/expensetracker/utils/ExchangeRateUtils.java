package org.theronin.expensetracker.utils;

import org.theronin.expensetracker.model.ExchangeRate;

import java.util.Comparator;

public class ExchangeRateUtils {
    public static Comparator<ExchangeRate> comparator() {
        return new Comparator<ExchangeRate>() {
            @Override
            public int compare(ExchangeRate lhs, ExchangeRate rhs) {
                String lhDate = DateUtils.getStorageFormattedDate(lhs.utcDate);
                String rhDate = DateUtils.getStorageFormattedDate(rhs.utcDate);
                int byDate = lhDate.compareTo(rhDate);
                if (byDate == 0) {
                    return lhs.currencyCode.compareTo(rhs.currencyCode);
                } else {
                    return byDate;
                }
            }
        };
    }
}
