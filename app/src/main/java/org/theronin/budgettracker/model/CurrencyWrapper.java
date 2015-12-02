package org.theronin.budgettracker.model;

import android.database.Cursor;

import org.theronin.budgettracker.data.BudgetContract.CurrenciesTable;

import java.util.Currency;

public class CurrencyWrapper {

    public final Currency currency;
    public final String code;
    public final String symbol;

    public CurrencyWrapper(String code, String symbol) {
        this.currency = Currency.getInstance(code);
        this.code = code;
        this.symbol = symbol;
    }

    public static CurrencyWrapper fromCursor(Cursor cursor) {
        return new CurrencyWrapper(
                cursor.getString(CurrenciesTable.INDEX_CODE),
                cursor.getString(CurrenciesTable.INDEX_SYMBOL));
    }
}
