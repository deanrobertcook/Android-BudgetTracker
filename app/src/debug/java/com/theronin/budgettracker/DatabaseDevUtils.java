package com.theronin.budgettracker;

import android.content.ContentValues;
import android.database.Cursor;

import com.theronin.budgettracker.data.BudgetContract;
import com.theronin.budgettracker.data.BudgetDbHelper;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.model.Entry;

import java.util.Random;

public class DatabaseDevUtils {

    public static void clearDatabase(BudgetDbHelper dbHelper) {
        //For some reason, using context.deleteDatabase() spoils the database for subsequent tests
        //instead, it's better to just drop the tables and recreate everything
        dbHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + BudgetContract.EntriesTable.TABLE_NAME);
        dbHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " +
                BudgetContract.CategoriesTable.TABLE_NAME);
        dbHelper.onCreate(dbHelper.getWritableDatabase());
    }

    public static long insertCategoryDirectlyToDatabase(BudgetDbHelper dbHelper, Category
            category) {
        ContentValues values = new ContentValues();
        values.put(BudgetContract.CategoriesTable.COL_CATEGORY_NAME, category.name);
        if (category.date != null) {
            values.put(BudgetContract.CategoriesTable.COL_DATE_CREATED, category.date);
        }

        long categoryId = dbHelper.getWritableDatabase().insert(BudgetContract.CategoriesTable
                        .TABLE_NAME, null,
                values);
        if (categoryId == -1) {
            throw new RuntimeException("An error occurred inserting the Category into the DB");
        }
        return categoryId;
    }


    public static long findCategoryId(BudgetDbHelper dbHelper, String categoryName) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                BudgetContract.CategoriesTable.TABLE_NAME,
                new String[]{BudgetContract.CategoriesTable._ID},
                BudgetContract.CategoriesTable.COL_CATEGORY_NAME + "= ?",
                new String[]{categoryName},
                null, null, null
        );

        cursor.moveToFirst();
        long categoryId =  cursor.getLong(0);
        cursor.close();
        return categoryId;
    }

    public static String findCategoryName(BudgetDbHelper dbHelper, long id) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                BudgetContract.CategoriesTable.TABLE_NAME,
                new String[]{BudgetContract.CategoriesTable.COL_CATEGORY_NAME},
                BudgetContract.CategoriesTable._ID + "= ?",
                new String[]{Long.toString(id)},
                null, null, null
        );

        cursor.moveToFirst();
        String categoryName =  cursor.getString(0);
        cursor.close();
        return categoryName;
    }

    public static long insertEntryDirectlyToDatabase(BudgetDbHelper dbHelper, Entry entry) {
        ContentValues values = new ContentValues();
        values.put(BudgetContract.EntriesTable.COL_CATEGORY_ID, findCategoryId(dbHelper, entry
                .categoryName));
        values.put(BudgetContract.EntriesTable.COL_DATE_ENTERED, entry.dateEntered);
        values.put(BudgetContract.EntriesTable.COL_AMOUNT_CENTS, entry.amount);

        long entryId = dbHelper.getWritableDatabase().insert(BudgetContract.EntriesTable
                .TABLE_NAME, null, values);
        if (entryId == -1) {
            throw new RuntimeException("An error occurred inserting the Entry into the DB");
        }
        return entryId;
    }

    public static void fillDatabaseWithDummyData(BudgetDbHelper dbHelper, String[] categoryNames, int numEntries, int maxAmount) {
        Random random = new Random();

        for (String categoryName : categoryNames) {
            insertCategoryDirectlyToDatabase(dbHelper, new Category(categoryName));
        }

        for (int i = 0; i < numEntries; i++) {
            insertEntryDirectlyToDatabase(
                    dbHelper,
                    new Entry(
                            categoryNames[random.nextInt(categoryNames.length)],
                            DateDevUtils.getRandomDate(),
                            random.nextInt(maxAmount))
            );
        }
    }
}
