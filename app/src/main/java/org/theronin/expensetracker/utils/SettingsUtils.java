package org.theronin.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.theronin.expensetracker.R;

public class SettingsUtils {
    public static long getMonthlyLimit(Context context) {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = defaultPreferences.getString(context.getString(R.string.pref_monthly_limit_key), "0");
        return value.isEmpty() ? 0 : Long.parseLong(value);
    }
}
