package com.theronin.budgettracker.model;

import android.support.test.runner.AndroidJUnit4;

import com.theronin.budgettracker.TestUtils;
import com.theronin.budgettracker.utils.DateUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CategoryTest {

    @Test
    public void monthlyAverage_ZeroTotal() {
        int monthsAgo = 3;
        long total = 0;
        long expectedAverage = total / monthsAgo;
        String dateCreated = TestUtils.getDaysAgo((int) (monthsAgo * DateUtils.AVG_DAYS_IN_MONTH));

        Category category = new Category(-1, "test", dateCreated, total);

        assertEquals(expectedAverage, category.getMonthlyAverage());

    }

    @Test
    public void monthlyAverage_LargeTotal() {
        int monthsAgo = 3;
        long total = 1000000;
        long expectedAverage = total / monthsAgo;
        String dateCreated = TestUtils.getDaysAgo((int) (monthsAgo * DateUtils.AVG_DAYS_IN_MONTH));

        Category category = new Category(-1, "test", dateCreated, total);

        assertEquals(expectedAverage, category.getMonthlyAverage());
    }
}
