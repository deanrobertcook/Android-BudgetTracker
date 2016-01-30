package org.theronin.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.theronin.expensetracker.R;

public class SettingsUtils {
    public static long getMonthlyLimit(Context context) {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Long.parseLong(defaultPreferences.getString(context.getString(R.string.pref_monthly_limit_key), "0"));
    }
}
