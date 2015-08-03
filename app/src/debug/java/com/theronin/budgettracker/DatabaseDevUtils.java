package com.theronin.budgettracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.theronin.budgettracker.data.BudgetContractV2;
import com.theronin.budgettracker.data.BudgetDbHelper;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.model.Entry;

import java.util.Random;

public class DatabaseDevUtils {

    public static void clearDatabase(BudgetDbHelper dbHelper) {
        clearDatabase(dbHelper.getWritableDatabase());
    }

    public static void clearDatabase(SQLiteDatabase database) {
        //For some reason, using context.deleteDatabase() spoils the database for subsequent tests
        //instead, it's better to just drop the tables and recreate everything
        database.execSQL("DROP TABLE IF EXISTS " + BudgetContractV2.EntriesTable.TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " +
                BudgetContractV2.CategoriesTable.TABLE_NAME);
    }

    public static long insertCategoryDirectlyToDatabase(BudgetDbHelper dbHelper, Category
            category) {
        return insertCategoryDirectlyToDatabase(dbHelper.getWritableDatabase(), category);
    }

    public static long insertCategoryDirectlyToDatabase(SQLiteDatabase database, Category
            category) {
        ContentValues values = new ContentValues();
        values.put(BudgetContractV2.CategoriesTable.COL_CATEGORY_NAME, category.name);
        if (category.date != null) {
            values.put(BudgetContractV2.CategoriesTable.COL_FIRST_ENTRY_DATE, category.date);
        }

        long categoryId = database.insert(BudgetContractV2.CategoriesTable
                        .TABLE_NAME, null,
                values);
        if (categoryId == -1) {
            throw new RuntimeException("An error occurred inserting the Category into the DB");
        }
        return categoryId;
    }

    public static long findCategoryId(BudgetDbHelper dbHelper, String categoryName) {
        return findCategoryId(dbHelper.getWritableDatabase(), categoryName);
    }

    public static long findCategoryId(SQLiteDatabase database, String categoryName) {
        Cursor cursor = database.query(
                BudgetContractV2.CategoriesTable.TABLE_NAME,
                new String[]{BudgetContractV2.CategoriesTable._ID},
                BudgetContractV2.CategoriesTable.COL_CATEGORY_NAME + "= ?",
                new String[]{categoryName},
                null, null, null
        );

        cursor.moveToFirst();
        long categoryId =  cursor.getLong(0);
        cursor.close();
        return categoryId;
    }

    public static String findCategoryName(BudgetDbHelper dbHelper, long id) {
        return findCategoryName(dbHelper.getWritableDatabase(), id);
    }

    public static String findCategoryName(SQLiteDatabase database, long id) {
        Cursor cursor = database.query(
                BudgetContractV2.CategoriesTable.TABLE_NAME,
                new String[]{BudgetContractV2.CategoriesTable.COL_CATEGORY_NAME},
                BudgetContractV2.CategoriesTable._ID + "= ?",
                new String[]{Long.toString(id)},
                null, null, null
        );

        cursor.moveToFirst();
        String categoryName =  cursor.getString(0);
        cursor.close();
        return categoryName;
    }

    public static long insertEntryDirectlyToDatabase(BudgetDbHelper dbHelper, Entry entry) {
        return insertEntryDirectlyToDatabase(dbHelper.getWritableDatabase(), entry);
    }

    public static long insertEntryDirectlyToDatabase(SQLiteDatabase database, Entry entry) {
        ContentValues values = new ContentValues();
        values.put(BudgetContractV2.EntriesTable.COL_CATEGORY_ID, findCategoryId(database, entry
                .categoryName));
        values.put(BudgetContractV2.EntriesTable.COL_DATE_ENTERED, entry.dateEntered);
        values.put(BudgetContractV2.EntriesTable.COL_AMOUNT_CENTS, entry.amount);

        long entryId = database.insert(BudgetContractV2.EntriesTable
                .TABLE_NAME, null, values);
        if (entryId == -1) {
            throw new RuntimeException("An error occurred inserting the Entry into the DB");
        }
        return entryId;
    }

    public static void fillDatabaseWithDummyData(BudgetDbHelper dbHelper, String[] categoryNames, int numEntries, int maxAmount) {
        fillDatabaseWithDummyData(dbHelper.getWritableDatabase(), categoryNames, numEntries, maxAmount);
    }

    public static void fillDatabaseWithDummyData(SQLiteDatabase database, String[] categoryNames, int numEntries, int maxAmount) {
        Random random = new Random();

        for (String categoryName : categoryNames) {
            insertCategoryDirectlyToDatabase(database, new Category(categoryName));
        }

        for (int i = 0; i < numEntries; i++) {
            insertEntryDirectlyToDatabase(
                    database,
                    new Entry(
                            categoryNames[random.nextInt(categoryNames.length)],
                            DateDevUtils.getRandomDate(),
                            random.nextInt(maxAmount))
            );
        }
    }
}
