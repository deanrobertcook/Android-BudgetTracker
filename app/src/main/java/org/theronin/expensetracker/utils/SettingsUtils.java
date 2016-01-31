package org.theronin.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.SupportedCurrencies;
import org.theronin.expensetracker.model.Currency;

public class SettingsUtils {

    public static Currency getHomeCurrency(Context context) {
        return getCurrency(R.string.pref_home_currency_key, context);
    }

    public static Currency getCurrentCurrency(Context context) {
        return getCurrency(R.string.pref_current_currency_key, context);
    }

    private static Currency getCurrency(int currencyKeyResourceId, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultCurrency = context.getString(R.string.pref_currency_default);
        String currentCurrencyKey = context.getString(currencyKeyResourceId);

        String currencyCode = preferences.getString(currentCurrencyKey, defaultCurrency);

        return new SupportedCurrencies().findCurrency(currencyCode);
    }

    public static long getMonthlyLimit(Context context) {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = defaultPreferences.getString(context.getString(R.string.pref_monthly_limit_key), "0");
        return value.isEmpty() ? 0 : Long.parseLong(value);
    }
}
