package org.theronin.expensetracker.model;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.theronin.expensetracker.testutils.Constants.JAN_1_2000;
import static org.theronin.expensetracker.testutils.Constants.JAN_2_2000;

public class ExchangeRateTest {

    @Test @SmallTest
    public void equalsMethodShouldReturnTrueForExchangeRatesWithSameDateAndCode() {
        ExchangeRate rate1 = new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0);
        ExchangeRate rate2 = new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0);

        assertTrue(rate1.equals(rate2));
    }

    @Test @SmallTest
    public void equalsMethodShouldReturnFalseIfDateDiffers() {
        ExchangeRate rate1 = new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0);
        ExchangeRate rate2 = new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0);

        assertFalse(rate1.equals(rate2));
    }

    @Test @SmallTest
    public void equalsMethodShouldReturnFalseIfCodeDiffers() {
        ExchangeRate rate1 = new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0);
        ExchangeRate rate2 = new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0);

        assertFalse(rate1.equals(rate2));
    }

    @Test @SmallTest
    public void testSortingByDate() {
        List<ExchangeRate> list = Arrays.asList(
                new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0));

        Collections.sort(list);

        assertEquals(list.get(0).utcDate, JAN_2_2000);
        assertEquals(list.get(1).utcDate, JAN_1_2000);
    }

    @Test @SmallTest
    public void testSortingByCode() {
        List<ExchangeRate> list = Arrays.asList(
                new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0),
                new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0));

        Collections.sort(list);

        assertEquals(list.get(0).currencyCode, "AUD");
        assertEquals(list.get(1).currencyCode, "EUR");
    }

}