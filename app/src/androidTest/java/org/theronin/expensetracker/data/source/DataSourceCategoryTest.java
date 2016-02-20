package org.theronin.expensetracker.data.source;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.testutils.InMemoryDataSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.theronin.expensetracker.testutils.Constants.LATCH_WAIT;

public class DataSourceCategoryTest {

    private AbsDataSource<Category> categoryAbsDataSource;
    private AbsDataSource<Entry> entryAbsDataSource;

    @Before
    public void setup() {
        InMemoryDataSource dataSource = new InMemoryDataSource();
        categoryAbsDataSource = dataSource.getCategoryDataSource();
        entryAbsDataSource = dataSource.getEntryDataSource();
    }

    @Test
    @SmallTest
    public void observersAreNotifiedOnDataInsert() throws InterruptedException {
        final CountDownLatch callbackLatch = new CountDownLatch(1);
        categoryAbsDataSource.registerObserver(new AbsDataSource.Observer() {
            @Override
            public void onDataSourceChanged() {
                callbackLatch.countDown();
            }
        });

        categoryAbsDataSource.insert(new Category("test"));

        callbackLatch.await(LATCH_WAIT, TimeUnit.MILLISECONDS);
        assertEquals("Observer was not notified", 0, callbackLatch.getCount());
    }

    @Test
    @SmallTest
    public void editCategoryName() {
        String originalName = "test";
        String newName = "test2";

        Category category = categoryAbsDataSource.insert(new Category(originalName));
        category.setName(newName);
        categoryAbsDataSource.update(category);

        List<Category> savedCategories = categoryAbsDataSource.query();

        assertEquals(1, savedCategories.size());
        assertEquals(newName, savedCategories.get(0).getName());
    }

    @Test
    @SmallTest
    public void attemptToEditCategoryWithAlreadyExistingNameShouldDoNothing() {
        String conflictingName = "test1";
        String oldName = "test2";
        categoryAbsDataSource.insert(new Category(conflictingName));
        Category category = categoryAbsDataSource.insert(new Category(oldName));

        category.setName(conflictingName);
        assertFalse(categoryAbsDataSource.update(category));

        List<Category> savedCategories = categoryAbsDataSource.query();
        assertEquals(2, savedCategories.size());
        assertEquals(oldName, savedCategories.get(1).getName());
    }

    @Test
    @SmallTest
    public void updatingCategoryShouldTriggerEntrySourceObserver() throws InterruptedException {
        String categoryName = "test1";
        String newName = "test2";
        //add a few entries
        entryAbsDataSource.bulkInsert(Arrays.asList(
                new Entry(0, 0, new Category(categoryName), new Currency("AUD")),
                new Entry(0, 0, new Category(categoryName), new Currency("AUD"))
        ));

        //get the automatically created category
        Category category = categoryAbsDataSource.query().get(0);

        //register observer to entry datasource
        final CountDownLatch latch = new CountDownLatch(1);
        entryAbsDataSource.registerObserver(new AbsDataSource.Observer() {
            @Override
            public void onDataSourceChanged() {
                latch.countDown();
            }
        });

        //update category name:
        category.setName(newName);
        assertTrue(categoryAbsDataSource.update(category));

        latch.await(LATCH_WAIT, TimeUnit.MILLISECONDS);
        assertEquals("Observer was not notified", 0, latch.getCount());

        //check that the entries now have the new category name
        List<Entry> entries = entryAbsDataSource.query();
        for (Entry entry : entries) {
            assertEquals(newName, entry.category.getName());
        }

    }
}
