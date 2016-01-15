package org.theronin.expensetracker.utils;

import junit.framework.Assert;

import org.junit.Test;
import org.theronin.expensetracker.model.ExchangeRate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExchangeRateUtilsTest {

    private static final long JAN_1_2000 = 946681201000L; //2000-01-01
    private static final long JAN_2_2000 = JAN_1_2000 + 86400000L; //2000-01-02

    @Test
    public void testSortingByDate() {
        List<ExchangeRate> list = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0));

        Collections.sort(list, ExchangeRateUtils.comparator());

        Assert.assertEquals(list.get(0).utcDate, JAN_1_2000);
        Assert.assertEquals(list.get(1).utcDate, JAN_2_2000);
    }

    @Test
    public void testSortingByCode() {
        List<ExchangeRate> list = Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0));

        Collections.sort(list, ExchangeRateUtils.comparator());

        Assert.assertEquals(list.get(0).currencyCode, "AUD");
        Assert.assertEquals(list.get(1).currencyCode, "EUR");
    }
}