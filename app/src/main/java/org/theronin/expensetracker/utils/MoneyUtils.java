package org.theronin.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Currency;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

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

    public static String getDisplay(Context context, long cents) {
        return getDisplay(context, cents, false);
    }

    public static String getDisplayCompact(Context context, long cents) {
        return getDisplay(context, cents, true);
    }

    private static final NavigableMap<Long, Integer> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000_00L, R.string.suffix_thousand);
        suffixes.put(1_000_000_00L, R.string.suffix_million);
        suffixes.put(1_000_000_000_00L, R.string.suffix_billion);
    }

    private static String getDisplay(Context context, long cents, boolean compact) {
        if (cents < 0) {
            return "-.--";
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumFractionDigits(2);

        String suffix = "";
        if (compact) {
            if (cents >= 10_000_00L) {
                Map.Entry<Long, Integer> mapEntry = suffixes.floorEntry(cents);
                suffix = context.getString(mapEntry.getValue());
                cents = cents / (mapEntry.getKey() / 100);
                numberFormat.setMaximumFractionDigits(1);

            } else if (cents >= 100_00L) {
                numberFormat.setMaximumFractionDigits(0);
            }
        }

        BigDecimal centsFormatted = new BigDecimal(Long.toString(cents)).setScale(2, BigDecimal.ROUND_FLOOR);
        BigDecimal parsed = centsFormatted.divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
        return numberFormat.format((parsed)) + suffix;
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
