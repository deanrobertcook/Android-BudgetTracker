package org.theronin.expensetracker.utils;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class DateUtilsTest {

    @Test
    public void twoTimestampsInSameMonthShouldFailSameMonthCheck() {
        long time1 = 1422720000000L; //2015-01-31 17:00
        long time2 = 1422806400000L; //2015-02-01 17:00

        assertFalse(DateUtils.sameMonth(time1, time2));
    }

    @Test
    public void twoTimestampsInSameMonthShouldPassSameMonthCheck() {
        long time1 = 1422720000000L; //2015-01-31 17:00
        long time2 = 1420128000000L; //2015-01-01 17:00

        assertTrue(DateUtils.sameMonth(time1, time2));
    }
}