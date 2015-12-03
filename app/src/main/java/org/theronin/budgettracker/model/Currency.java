package org.theronin.budgettracker.model;

import android.database.Cursor;

import org.theronin.budgettracker.data.BudgetContract.CurrenciesTable;

public class Currency {

    public final long id;
    public final String code;
    public final String symbol;

    public Currency(String code) {
        this(-1, code, null);
    }

    public Currency(String code,
                    String symbol) {
        this(-1, code, symbol);
    }

    public Currency(long id,
                    String code,
                    String symbol) {
        this.id = id;
        this.code = code;
        this.symbol = symbol;
    }

    public static Currency fromCursor(Cursor cursor) {
        return new Currency(
                cursor.getLong(CurrenciesTable.INDEX_ID),
                cursor.getString(CurrenciesTable.INDEX_CODE),
                cursor.getString(CurrenciesTable.INDEX_SYMBOL));
    }
}
