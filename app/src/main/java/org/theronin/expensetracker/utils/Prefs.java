package org.theronin.expensetracker.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.SupportedCurrencies;
import org.theronin.expensetracker.model.Currency;

public class Prefs {

    private static final String PREF_LAST_SYNC_CHECK_KEY = "PREF_LAST_SYNC_CHECK";

    public static Currency getHomeCurrency(Context context) {
        return getCurrency(R.string.pref_home_currency_key, context);
    }

    public static Currency getCurrentCurrency(Context context) {
        return getCurrency(R.string.pref_current_currency_key, context);
    }

    private static Currency getCurrency(int currencyKeyResourceId, Context context) {
        String currencyCode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(currencyKeyResourceId), context.getString(R.string.pref_currency_default));

        return new SupportedCurrencies().findCurrency(currencyCode);
    }

    public static long getMonthlyLimit(Context context) {
        String value = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_monthly_limit_key), "0");
        return value.isEmpty() ? 0 : Long.parseLong(value);
    }

    public static boolean hasWelcomeScreenBeenShown(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_welcome_screen_has_been_shown), false);
    }

    public static void setWelcomeScreenShown(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(context.getString(R.string.pref_welcome_screen_has_been_shown), true).apply();
    }

    public static long getLastSyncTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(PREF_LAST_SYNC_CHECK_KEY, -1);
    }

    public static void setLastSyncTime(long lastSyncTime, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(PREF_LAST_SYNC_CHECK_KEY, lastSyncTime).apply();
    }
}
