package org.theronin.expensetracker.utils;

import org.theronin.expensetracker.model.ExchangeRate;

import java.util.Comparator;

public class ExchangeRateUtils {
    public static Comparator<ExchangeRate> comparator() {
        return new Comparator<ExchangeRate>() {
            @Override
            public int compare(ExchangeRate lhs, ExchangeRate rhs) {
                return (int) (lhs.utcDate - rhs.utcDate);
            }
        };
    }
}
