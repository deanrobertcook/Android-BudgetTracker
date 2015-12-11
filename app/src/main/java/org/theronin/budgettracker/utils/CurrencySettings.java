package org.theronin.budgettracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.theronin.budgettracker.model.Currency;

public class CurrencySettings implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;
    private Listener listener;

    private SharedPreferences defaultPreferences;
    private Currency homeCurrency;

    public CurrencySettings(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;

        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        defaultPreferences.registerOnSharedPreferenceChangeListener(this);
        homeCurrency = MoneyUtils.getHomeCurrency(context, defaultPreferences);
    }

    public Currency getHomeCurrency() {
        return homeCurrency;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        homeCurrency = MoneyUtils.getHomeCurrency(context, defaultPreferences);
        listener.onHomeCurrencyChanged(homeCurrency);
    }

    public interface Listener {
        void onHomeCurrencyChanged(Currency homeCurrency);
    }
}
