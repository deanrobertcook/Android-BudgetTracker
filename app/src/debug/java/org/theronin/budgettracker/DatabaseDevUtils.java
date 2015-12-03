package org.theronin.budgettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.theronin.budgettracker.data.BudgetContract;
import org.theronin.budgettracker.data.BudgetContract.EntriesTable;
import org.theronin.budgettracker.data.BudgetDbHelper;
import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Entry;

import java.util.Random;

public class DatabaseDevUtils {
    public static final String[] SOME_CATEGORIES = {"cashews", "apples", "bananas",
            "tissues", "beer", "electronics", "schawarma", "pens", "paper", "train tickets"};


    public static void resetDatabase(SQLiteDatabase database) {
        BudgetDbHelper.dropTables(database);
        BudgetDbHelper.createTables(database);
    }

    public static long insertCategoryDirectlyToDatabase(BudgetDbHelper dbHelper, Category
            category) {
        return insertCategoryDirectlyToDatabase(dbHelper.getWritableDatabase(), category);
    }

    public static long insertCategoryDirectlyToDatabase(SQLiteDatabase database, Category
            category) {
        ContentValues values = new ContentValues();
        values.put(BudgetContract.CategoriesView.COL_CATEGORY_NAME, category.name);
        if (category.utcFirstEntryDate != 0) {
            values.put(BudgetContract.CategoriesView.COL_FIRST_ENTRY_DATE, category.utcFirstEntryDate);
        }

        long categoryId = database.insert(BudgetContract.CategoriesView
                        .VIEW_NAME, null,
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
                BudgetContract.CategoriesView.VIEW_NAME,
                new String[]{BudgetContract.CategoriesView._ID},
                BudgetContract.CategoriesView.COL_CATEGORY_NAME + "= ?",
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
                BudgetContract.CategoriesView.VIEW_NAME,
                new String[]{BudgetContract.CategoriesView.COL_CATEGORY_NAME},
                BudgetContract.CategoriesView._ID + "= ?",
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

    /**
     * Inserts an Entry into the database and also handles searching for the Category ID
     * @param database
     * @param entry
     * @return
     */
    public static long insertEntryDirectlyToDatabase(SQLiteDatabase database, Entry entry) {
        ContentValues values = getEntryValues(database, entry);

        long entryId = database.insert(EntriesTable
                .TABLE_NAME, null, values);
        if (entryId == -1) {
            throw new RuntimeException("An error occurred inserting the Entry into the DB");
        }
        return entryId;
    }

    public static ContentValues getEntryValues(SQLiteDatabase database, Entry entry) {
        ContentValues values = new ContentValues();

        return values;
    }

    public static void fillDatabaseWithDummyData(SQLiteDatabase database, String[] categoryNames, int numEntries, int maxAmount) {
        Random random = new Random();

        for (String categoryName : categoryNames) {
            insertCategoryDirectlyToDatabase(database, new Category(categoryName));
        }

//        for (int i = 0; i < numEntries; i++) {
//            insertEntryDirectlyToDatabase(
//                    database,
//                    new Entry(
//                            categoryNames[random.nextInt(categoryNames.length)],
//                            DateDevUtils.getRandomDate(),
//                            random.nextInt(maxAmount))
//            );
//        }
    }

    public static void fillDatabaseUsingContentProvider(Context context, String[] categoryNames, int numEntries, int maxAmount) {
        Random random = new Random();
        BudgetDbHelper helper = new BudgetDbHelper(context);

        for (String categoryName : categoryNames) {
            insertCategoryDirectlyToDatabase(helper.getWritableDatabase(), new Category(categoryName));
        }

//        for (int i = 0; i < numEntries; i++) {
//            insertEntryUsingContentProvider(
//                    context,
//                    new Entry(
//                            categoryNames[random.nextInt(categoryNames.length)],
//                            DateDevUtils.getRandomDate(),
//                            random.nextInt(maxAmount))
//            );
//        }
    }
}
