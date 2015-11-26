package org.theronin.budgettracker.utils;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MoneyUtilsTest {

    /**
     * CENTS TESTS
     */

    @Test
    public void convertToCents_Zero() {
        String testAmount = "0.00";
        long expectedAmount = 0;
        long returnedAmount = MoneyUtils.convertToCents(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    @Test
    public void convertToCents_LessThan10RoundNumber() {
        String testAmount = "1.00";
        long expectedAmount = 100;
        long returnedAmount = MoneyUtils.convertToCents(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    @Test
    public void convertToCents_MoreThan100() {
        String testAmount = "100.00";
        long expectedAmount = 10000;
        long returnedAmount = MoneyUtils.convertToCents(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    @Test
    public void convertToCents_SingleDecimal() {
        String testAmount = "100.1";
        long expectedAmount = 10010;
        long returnedAmount = MoneyUtils.convertToCents(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    @Test
    public void convertToCents_WithoutDecimal() {
        String testAmount = "100";
        long expectedAmount = 10000;
        long returnedAmount = MoneyUtils.convertToCents(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    /**
     * DOLLARS TESTS
     */
    @Test
    public void convertToDollars_InvalidAmount() {
        long testAmount = -1;
        String expectedAmount = "-.--";
        String returnedAmount = MoneyUtils.convertToDollars(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    @Test
    public void convertToDollars_Zero() {
        long testAmount = 0;
        String expectedAmount = "0.00";
        String returnedAmount = MoneyUtils.convertToDollars(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    @Test
    public void convertToDollars_under10() {
        long testAmount = 500;
        String expectedAmount = "5.00";
        String returnedAmount = MoneyUtils.convertToDollars(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    @Test
    public void convertToDollars_above100() {
        long testAmount = 12000;
        String expectedAmount = "120.00";
        String returnedAmount = MoneyUtils.convertToDollars(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    @Test
    public void convertToDollars_centsLessThan10() {
        long testAmount = 5005;
        String expectedAmount = "50.05";
        String returnedAmount = MoneyUtils.convertToDollars(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }

    @Test
    public void convertToDollars_centsAbove10() {
        long testAmount = 399;
        String expectedAmount = "3.99";
        String returnedAmount = MoneyUtils.convertToDollars(testAmount);
        assertEquals(expectedAmount, returnedAmount);
    }
}
