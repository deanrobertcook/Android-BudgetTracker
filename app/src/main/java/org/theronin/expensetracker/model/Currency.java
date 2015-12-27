package org.theronin.expensetracker.model;

import android.content.ContentValues;

import com.parse.ParseObject;

import org.apache.commons.lang.NotImplementedException;

public class Currency extends Entity {

    public final String code;
    public final String symbol;
    public final String name;

    public Currency(String code) {
        this(-1, code, null, null);
    }

    public Currency(String code,
                    String symbol,
                    String name) {
        this(-1, code, symbol, name);
    }

    public Currency(long id,
                    String code,
                    String symbol) {
        this(id, code, symbol, null);
    }

    public Currency(long id,
                    String code,
                    String symbol,
                    String name) {
        this.id = id;
        this.code = code;
        this.symbol = symbol;
        this.name = name;
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
