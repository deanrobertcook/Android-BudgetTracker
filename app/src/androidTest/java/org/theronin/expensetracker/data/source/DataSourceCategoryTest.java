package org.theronin.expensetracker.data.source;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.testutils.InMemoryDataSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static org.theronin.expensetracker.testutils.Constants.DEFAULT_LATCH_WAIT;

public class DataSourceCategoryTest {
    @Test
    @SmallTest
    public void observersAreNotifiedOnDataInsert() throws InterruptedException {
        final CountDownLatch callbackLatch = new CountDownLatch(1);
        AbsDataSource<Category> categoryAbsDataSource = new InMemoryDataSource().getCategoryDataSource();


        AbsDataSource.Observer observer = new AbsDataSource.Observer() {
            @Override
            public void onDataSourceChanged() {
                callbackLatch.countDown();
            }
        };

        categoryAbsDataSource.registerObserver(observer);

        categoryAbsDataSource.insert(new Category("test"));

        callbackLatch.await(DEFAULT_LATCH_WAIT, TimeUnit.MILLISECONDS);
        assertEquals("Observer was not notified", 0, callbackLatch.getCount());
    }
}
