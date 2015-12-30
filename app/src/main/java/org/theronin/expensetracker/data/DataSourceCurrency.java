package org.theronin.expensetracker.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang.NotImplementedException;
import org.theronin.expensetracker.data.Contract.CurrencyTable;
import org.theronin.expensetracker.model.Currency;

import java.util.Collection;

public class DataSourceCurrency extends AbsDataSource<Currency> {

    public DataSourceCurrency(Context context, DbHelper dbHelper) {
        super(context, dbHelper);
    }

    @Override
    protected String getTableName() {
        return CurrencyTable.TABLE_NAME;
    }

    @Override
    protected String[] getQueryProjection() {
        return CurrencyTable.PROJECTION;
    }

    @Override
    public Currency fromCursor(Cursor cursor) {
        return new Currency(
                cursor.getLong(CurrencyTable.INDEX_ID),
                cursor.getString(CurrencyTable.INDEX_CODE),
                cursor.getString(CurrencyTable.INDEX_SYMBOL));
    }

    @Override
    protected long insertOperation(SQLiteDatabase db, Currency entity) {
        throw new NotImplementedException("Cannot insert into Currencies table");
    }

    @Override
    protected int updateOperation(SQLiteDatabase db, Currency entity) {
        throw new NotImplementedException("Cannot update Currencies table");
    }

    @Override
    protected int deleteOperation(SQLiteDatabase sb, Collection<Currency> entities) {
        throw new NotImplementedException("Cannot delete from Currencies table");
    }

    @Override
    public long searchForEntityIdBy(String columnName, String searchValue) {
        long id = super.searchForEntityIdBy(columnName, searchValue);
        if (id == -1) {
            throw new IllegalArgumentException(
                    String.format("The currency %s is not supported", searchValue)
            );
        }
        return id;
    }
}
