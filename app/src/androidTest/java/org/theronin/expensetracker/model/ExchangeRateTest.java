package org.theronin.expensetracker.model;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ExchangeRateTest {

    private static final long JAN_1_2000 = 946681201000L; //2000-01-01
    private static final long JAN_2_2000 = JAN_1_2000 + 86400000L; //2000-01-02

    @Test
    public void equalsMethodShouldReturnTrueForExchangeRatesWithSameDateAndCode() {
        ExchangeRate rate1 = new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0);
        ExchangeRate rate2 = new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0);

        assertTrue(rate1.equals(rate2));
    }

    @Test
    public void equalsMethodShouldReturnFalseIfDateDiffers() {
        ExchangeRate rate1 = new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0);
        ExchangeRate rate2 = new ExchangeRate(-1, "AUD", JAN_2_2000, -1, -1, 0);

        assertFalse(rate1.equals(rate2));
    }

    @Test
    public void equalsMethodShouldReturnFalseIfCodeDiffers() {
        ExchangeRate rate1 = new ExchangeRate(-1, "AUD", JAN_1_2000, -1, -1, 0);
        ExchangeRate rate2 = new ExchangeRate(-1, "EUR", JAN_1_2000, -1, -1, 0);

        assertFalse(rate1.equals(rate2));
    }

}