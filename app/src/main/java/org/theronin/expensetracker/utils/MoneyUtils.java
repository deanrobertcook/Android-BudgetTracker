package org.theronin.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.SupportedCurrencies;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
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

        String currencyCode = defaultPreferences.getString(currentCurrencyKey, defaultCurrency);

        return new SupportedCurrencies().findCurrency(currencyCode);
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
            return context.getString(R.string.amount_placeholder);
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

    public static class EntrySum {
        public final long amount;
        public final int missingEntries;

        public EntrySum(long amount, int missingEntries) {
            this.amount = amount;
            this.missingEntries = missingEntries;
        }
    }

    public interface EntryCondition {
        boolean check(Entry entry);
    }

    /**
     * Loops through the Collection of entries and calculates the total amount this list of
     * entries represents. The amount of each entry is only added to the total amount if some
     * predicate is true - that is the
     * {@link org.theronin.expensetracker.utils.MoneyUtils.EntryCondition#check(Entry)} method
     * returns true.
     *
     * The entries should have all of their exchange rate data set for a give home and current
     * currency. If any entries are missing their exchange rate data, then the number of missing
     * entries in the {@link org.theronin.expensetracker.utils.MoneyUtils.EntrySum} object will
     * be incremented, and the total sum will currently disregard the value of that entry.
     * @param entries The list of entries to sum over
     * @param condition a condition to test each entry against to see if it should be added to the
     *                  collection.
     * @return An EntrySum, representing the total amount given all of the entries that have
     * exchange rate data.
     */
    public static EntrySum calculateTotals(Collection<Entry> entries, EntryCondition condition) {
        long total = 0;
        int missingEntries = 0;
        for (Entry entry : entries) {
            if (condition == null || condition.check(entry)) {
                if (entry.getDirectExchangeRate() < 0) {
                    missingEntries++;
                } else {
                    total += Math.round((double) entry.amount * entry.getDirectExchangeRate());
                }
            }
        }
        return new EntrySum(total, missingEntries);
    }
}
