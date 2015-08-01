package com.theronin.budgettracker.utils;

import android.support.test.runner.AndroidJUnit4;

import com.theronin.budgettracker.TestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DateUtilsTest {

    @Test
    public void getDaysSinceToday_ShouldBeZero() {
        int daysAgo = 0;
        String daysAgoDate = TestUtils.getDaysAgo(daysAgo);
        long expectedDaysElapsed = daysAgo;
        long actualDaysElapsed = DateUtils.daysSince(daysAgoDate);
        assertEquals(expectedDaysElapsed, actualDaysElapsed);
    }

    @Test
    public void getDaysSince_2DaysAgo() {
        int daysAgo = 2;
        String daysAgoDate = TestUtils.getDaysAgo(daysAgo);
        long expectedDaysElapsed = daysAgo;
        long actualDaysElapsed = DateUtils.daysSince(daysAgoDate);
        assertEquals(expectedDaysElapsed, actualDaysElapsed);
    }

    @Test
    public void getDaysSince_1000DaysAgo() {
        int daysAgo = 1000;
        String daysAgoDate = TestUtils.getDaysAgo(daysAgo);
        long expectedDaysElapsed = daysAgo;
        long actualDaysElapsed = DateUtils.daysSince(daysAgoDate);
        assertEquals(expectedDaysElapsed, actualDaysElapsed);
    }

    @Test
    public void getDaysSince_OverFiveYearsAgo() {
        int daysAgo = 10000;
        String daysAgoDate = TestUtils.getDaysAgo(daysAgo);
        long expectedDaysElapsed = daysAgo;
        long actualDaysElapsed = DateUtils.daysSince(daysAgoDate);
        assertEquals(expectedDaysElapsed, actualDaysElapsed);
    }

}
