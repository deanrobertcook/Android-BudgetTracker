package org.theronin.expensetracker.model;

import android.content.ContentValues;

import com.parse.ParseObject;

import org.apache.commons.lang.NotImplementedException;

public class Currency extends Entity {

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

    @Override
    public ContentValues toValues() {
        throw new NotImplementedException("Currently no need to convert Currency into ContentValues");
    }

    @Override
    public ParseObject toParseObject() {
        throw new NotImplementedException();
    }
}
