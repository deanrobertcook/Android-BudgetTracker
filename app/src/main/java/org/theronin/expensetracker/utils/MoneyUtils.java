package org.theronin.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Currency;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;

public class MoneyUtils {

    public static Currency getHomeCurrency(Context context, SharedPreferences defaultPreferences) {
        return getCurrency(R.string.pref_home_currency_key, context, defaultPreferences);
    }

    public static Currency getCurrentCurrency(Context context, SharedPreferences defaultPreferences) {
        return getCurrency(R.string.pref_current_currency_key, context, defaultPreferences);
    }

    public static Currency getCurrency(int currencyKeyResourceId,
                                       Context context,
                                       SharedPreferences defaultPreferences) {
        String defaultCurrency = context.getString(R.string.pref_currency_default);
        String currentCurrencyKey = context.getString(currencyKeyResourceId);

        String[] currencyCodes = context.getResources().getStringArray(R.array.currency_codes);
        String[] currencySymbols = context.getResources().getStringArray(R.array.currency_symbols);

        String currencyCode = defaultPreferences.getString(currentCurrencyKey, defaultCurrency);
        int index = Arrays.asList(currencyCodes).indexOf(currencyCode);

        String currencySymbol = currencySymbols[index];
        return new Currency(currencyCode, currencySymbol);
    }

    public static String getDisplay(long cents) {
        return getDisplay(cents, false);
    }

    public static String getDisplayCompact(long cents) {
        return getDisplay(cents, true);
    }

    private static String getDisplay(long cents, boolean compact) {
        if (cents < 0) {
            return "-.--";
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumFractionDigits(2);

        String placeHolder = "";
        if (compact) {
            if (cents >= 100000000) {
                cents = cents / 1000000;
                placeHolder = "m";
                numberFormat.setMaximumFractionDigits(1);
            }
            if (cents >= 1000000) {
                cents = cents / 1000;
                placeHolder = "k";
                numberFormat.setMaximumFractionDigits(1);

            } else if (cents >= 10000) {
                numberFormat.setMaximumFractionDigits(0);
            }
        }

        BigDecimal centsFormatted = new BigDecimal(Long.toString(cents)).setScale(2, BigDecimal.ROUND_FLOOR);

        BigDecimal parsed = centsFormatted.divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);

        return numberFormat.format((parsed)) + placeHolder;
    }

    /**
     * Converts a non-compact amount in display format back into cents
     *
     * @param displayAmount The amount in display format e.g. ($1,000.00)
     * @return the amount in cents
     */
    public static long getCents(String displayAmount) {
        double result = 0;
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);

        try {
            result = numberFormat.parse(displayAmount).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return (Math.round(result * 100));
    }

}
